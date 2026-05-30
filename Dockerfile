FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy

WORKDIR /app

COPY target/ICoder-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]