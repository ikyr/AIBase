#!/bin/bash
# AIBase Deployment Script
# Usage: bash deploy.sh

set -e

SERVER="10.139.11.100"
SERVER_USER="root"
SERVER_PASSWORD="Dt1q2w3e4r!"
SERVER_DIR="/u01/aibase"
NODE_SSH="node .ssh-exec.js"
NODE_SCP="node .scp.js"

echo "=== Step 1: Verify Maven build artifacts ==="
MODULES="ai-base-gateway ai-base-knowledge ai-base-skill ai-base-workflow ai-base-agent ai-base-mcp-gateway ai-base-model-gateway ai-base-eval ai-base-platform"
for mod in $MODULES; do
  jar=$(ls $mod/target/$mod-*.jar 2>/dev/null | head -1)
  if [ -z "$jar" ]; then
    echo "ERROR: $mod JAR not found. Run 'mvn clean package -DskipTests' first."
    exit 1
  fi
  echo "  $mod: OK ($jar)"
done
echo ""

echo "=== Step 2: Create deployment archive ==="
ARCHIVE="/tmp/aibase-deploy-$(date +%Y%m%d%H%M%S).tar.gz"
tar -czf "$ARCHIVE" \
  docker-compose.yml \
  docker-compose/ \
  db/migrations/ \
  $MODULES/Dockerfile \
  $MODULES/target/*.jar \
  ai-base-frontend/Dockerfile \
  ai-base-frontend/nginx.conf \
  ai-base-frontend/package.json \
  ai-base-frontend/package-lock.json \
  ai-base-frontend/index.html \
  ai-base-frontend/vite.config.ts \
  ai-base-frontend/tsconfig.json \
  ai-base-frontend/tsconfig.node.json \
  ai-base-frontend/src/ \
  --exclude='*.orig' --exclude='*~' 2>/dev/null

echo "Archive: $ARCHIVE ($(du -h $ARCHIVE | cut -f1))"
echo ""

echo "=== Step 3: Transfer to server ==="
$NODE_SCP "$ARCHIVE" "$SERVER_DIR/aibase.tar.gz"
echo ""

echo "=== Step 4: Extract on server ==="
$NODE_SSH "$SERVER" "
  mkdir -p $SERVER_DIR && \
  cd $SERVER_DIR && \
  tar -xzf aibase.tar.gz && \
  rm aibase.tar.gz && \
  echo 'Extraction complete' && \
  ls -la $SERVER_DIR/
"
echo ""

echo "=== Step 5: Create .env file ==="
$NODE_SSH "$SERVER" "
  mkdir -p $SERVER_DIR/docker-compose && \
  cat > $SERVER_DIR/docker-compose/.env << 'ENVEOF'
# ---- 基础设施密码 ----
PG_PASSWORD=aibase_change_me
REDIS_PASSWORD=aibase_redis_change_me
MINIO_ROOT_PASSWORD=minioadmin_change_me
GRAFANA_PASSWORD=admin_change_me

# ---- PostgreSQL ----
PG_HOST=postgres
PG_PORT=5432
PG_DB=aibase
PG_USER=aibase

# ---- Redis ----
REDIS_HOST=redis
REDIS_PORT=6379

# ---- Nacos ----
NACOS_SERVER=nacos:8848
NACOS_NAMESPACE=

# ---- Milvus ----
MILVUS_HOST=milvus
MILVUS_PORT=19530
MILVUS_USERNAME=
MILVUS_PASSWORD=

# ---- RocketMQ ----
ROCKETMQ_NAMESRV=rocketmq-namesrv:9876

# ---- OpenTelemetry ----
OTEL_EXPORTER_OTLP_ENDPOINT=http://tempo:4317

# ---- AI 模型 ----
DASHSCOPE_API_KEY=sk-31d1fec50d20466e9081816ce8f853a1
OPENAI_API_KEY=
EMBEDDING_MODEL=text-embedding-v3

# ---- JVM ----
JAVA_OPTS=-Xms256m -Xmx512m

# ---- 前端 ----
FRONTEND_PORT=80
ENVEOF
  echo '.env file created'
"

echo "=== Deployment package ready ==="
echo "Run the following commands on the server ($SERVER_DIR):"
echo "  cd $SERVER_DIR"
echo "  docker compose -f docker-compose/infrastructure.yml up -d"
echo "  # Wait 30s for infrastructure to be ready"
echo "  # Initialize DB: run db/migrations/V*.sql against postgres"
echo "  docker compose -f docker-compose/app.yml up -d"
echo "  docker compose up -d frontend"
