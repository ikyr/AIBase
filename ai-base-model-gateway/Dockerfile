FROM eclipse-temurin:21-jre-alpine
COPY target/ai-base-model-gateway-*.jar app.jar
EXPOSE 8104
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
