CREATE TABLE IF NOT EXISTS mcp_server (
    id            VARCHAR(19)  PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    server_type   VARCHAR(50),
    transport     VARCHAR(50),
    endpoint      VARCHAR(500),
    description   TEXT,
    health_status VARCHAR(50)  NOT NULL DEFAULT 'UNKNOWN',
    status        VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS mcp_tool (
    id           VARCHAR(19)  PRIMARY KEY,
    server_id    VARCHAR(19)  NOT NULL REFERENCES mcp_server(id),
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    input_schema TEXT,
    status       VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS mcp_server_reg (
    id               VARCHAR(19)  PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    server_type      VARCHAR(50),
    transport        VARCHAR(50),
    endpoint         VARCHAR(500),
    auth_config      TEXT,
    tools_count      INTEGER NOT NULL DEFAULT 0,
    resources_count  INTEGER NOT NULL DEFAULT 0,
    prompts_count    INTEGER NOT NULL DEFAULT 0,
    health_status    VARCHAR(50)  NOT NULL DEFAULT 'UNKNOWN',
    last_health_check TIMESTAMP,
    status           VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS mcp_tool_reg (
    id               VARCHAR(19)  PRIMARY KEY,
    server_id        VARCHAR(19)  NOT NULL REFERENCES mcp_server_reg(id),
    tool_name        VARCHAR(200) NOT NULL,
    description      TEXT,
    input_schema     TEXT,
    tool_type        VARCHAR(50),
    source_service   VARCHAR(100),
    cache_ttl_seconds INTEGER,
    status           VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS mcp_client_conn (
    id              VARCHAR(19) PRIMARY KEY,
    server_id       VARCHAR(19) NOT NULL REFERENCES mcp_server_reg(id),
    status          VARCHAR(50) NOT NULL DEFAULT 'DISCONNECTED',
    session_token   VARCHAR(200),
    connected_at    TIMESTAMP,
    disconnected_at TIMESTAMP,
    error_count     INTEGER NOT NULL DEFAULT 0,
    last_error      TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS mcp_audit (
    id          VARCHAR(19) PRIMARY KEY,
    server_id   VARCHAR(19),
    tool_name   VARCHAR(200),
    session_id  VARCHAR(19),
    caller      VARCHAR(200),
    input       TEXT,
    output      TEXT,
    status      VARCHAR(50),
    duration_ms INTEGER,
    error_msg   TEXT,
    trace_id    VARCHAR(100),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mt_server ON mcp_tool(server_id);
CREATE INDEX IF NOT EXISTS idx_mtr_server ON mcp_tool_reg(server_id);
CREATE INDEX IF NOT EXISTS idx_mcc_server ON mcp_client_conn(server_id);
