# Build stage, using Alpine image to minimize layers
FROM maven:3.9-eclipse-temurin-21 AS builder
LABEL language="java"
LABEL framework="spring-boot"
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:+UseStringDeduplication -XX:+ExitOnOutOfMemoryError -XX:+UseContainerSupport"
ENV PORT=8080

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT}"]