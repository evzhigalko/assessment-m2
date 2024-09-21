FROM maven:3.8.5-amazoncorretto-17 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY consumer/pom.xml consumer/
COPY producer/pom.xml producer/

RUN mvn dependency:go-offline

COPY common/src common/src
COPY consumer/src consumer/src
COPY producer/src producer/src

RUN mvn clean package -DskipTests

FROM amazoncorretto:17
COPY --from=build /app/consumer/target/*.jar application.jar
EXPOSE 9002

ENTRYPOINT ["java", "-jar", "application.jar"]
