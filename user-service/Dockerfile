FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY user-service/pom.xml ./user-service/
RUN mvn dependency:go-offline -pl user-service -am -q
COPY user-service/src ./user-service/src
RUN mvn clean package -DskipTests -pl user-service -am -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
