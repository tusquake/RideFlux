FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY config-server/pom.xml ./config-server/
RUN mvn dependency:go-offline -pl config-server -am -q
COPY config-server/src ./config-server/src
COPY config-repo ./config-repo
RUN mvn clean package -DskipTests -pl config-server -am -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/config-server/target/*.jar app.jar
COPY --from=build /app/config-repo ./config-repo
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
