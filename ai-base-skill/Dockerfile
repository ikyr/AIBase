FROM eclipse-temurin:21-jre-alpine
COPY target/ai-base-skill-*.jar app.jar
EXPOSE 8102
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
