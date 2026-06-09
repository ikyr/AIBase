FROM eclipse-temurin:21-jre-alpine
COPY target/ai-base-workflow-*.jar app.jar
EXPOSE 8103
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
