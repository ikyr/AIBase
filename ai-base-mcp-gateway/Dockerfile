FROM eclipse-temurin:21-jre-alpine
COPY target/ai-base-mcp-gateway-*.jar app.jar
EXPOSE 8106
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
