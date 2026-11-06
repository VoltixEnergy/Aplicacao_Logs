FROM maven:4.0.0-rc-4-amazoncorretto-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/target/java-jira-integration-1.0-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]