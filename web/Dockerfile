FROM maven:3.9.8-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml ./
COPY core/ core/
COPY persistence/ persistence/
COPY web/ web/

RUN mvn -pl web -am package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=build /app/web/target/web-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
