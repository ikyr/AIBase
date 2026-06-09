FROM eclipse-temurin:21-jre-alpine
COPY target/ai-base-platform-*.jar app.jar
EXPOSE 8108
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
