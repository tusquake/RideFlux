FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY service-registry/pom.xml ./service-registry/
RUN mvn dependency:go-offline -pl service-registry -am -q
COPY service-registry/src ./service-registry/src
RUN mvn clean package -DskipTests -pl service-registry -am -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/service-registry/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
