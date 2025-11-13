# build stage
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# runtime stage
FROM eclipse-temurin:21-jdk-slim
ARG JAR_FILE=/app/target/*.jar
COPY --from=builder /app/target/*.jar /app/app.jar

ENV JAVA_OPTS=""
ENV PORT=8080

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT}"]