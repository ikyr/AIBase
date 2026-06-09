FROM eclipse-temurin:21-jre-alpine
COPY target/ai-base-eval-*.jar app.jar
EXPOSE 8107
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
