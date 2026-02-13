FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY ride-service/pom.xml ./ride-service/
RUN mvn dependency:go-offline -pl ride-service -am -q
COPY ride-service/src ./ride-service/src
RUN mvn clean package -DskipTests -pl ride-service -am -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/ride-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
