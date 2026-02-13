FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY pricing-service/pom.xml ./pricing-service/
RUN mvn dependency:go-offline -pl pricing-service -am -q
COPY pricing-service/src ./pricing-service/src
RUN mvn clean package -DskipTests -pl pricing-service -am -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/pricing-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
