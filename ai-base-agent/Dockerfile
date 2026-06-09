FROM eclipse-temurin:21-jre-alpine
COPY target/ai-base-agent-*.jar app.jar
EXPOSE 8105
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
