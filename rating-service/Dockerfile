FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY rating-service/pom.xml ./rating-service/
RUN mvn dependency:go-offline -pl rating-service -am -q
COPY rating-service/src ./rating-service/src
RUN mvn clean package -DskipTests -pl rating-service -am -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/rating-service/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
