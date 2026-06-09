FROM eclipse-temurin:21-jre-alpine
COPY target/ai-base-knowledge-*.jar app.jar
EXPOSE 8101
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
