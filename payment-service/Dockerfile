FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY payment-service/pom.xml ./payment-service/
RUN mvn dependency:go-offline -pl payment-service -am -q
COPY payment-service/src ./payment-service/src
RUN mvn clean package -DskipTests -pl payment-service -am -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/payment-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
