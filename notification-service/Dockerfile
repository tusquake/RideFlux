FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY notification-service/pom.xml ./notification-service/
RUN mvn dependency:go-offline -pl notification-service -am -q
COPY notification-service/src ./notification-service/src
RUN mvn clean package -DskipTests -pl notification-service -am -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
