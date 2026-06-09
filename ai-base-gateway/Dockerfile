FROM eclipse-temurin:21-jre-alpine
COPY target/ai-base-gateway-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
