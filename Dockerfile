FROM openjdk:17-oracle

WORKDIR /app

COPY target/Gateway-0.0.1-SNAPSHOT.jar .
COPY application.yml .

EXPOSE 8765

CMD ["java", "-jar", "Gateway-0.0.1-SNAPSHOT.jar", "--spring.config.location=application.yml"]