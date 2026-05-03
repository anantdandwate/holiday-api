FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/holiday-api-1.0.0.jar app.jar
COPY certs/truststore.jks certs/truststore.jks
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]