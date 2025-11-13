# build stage
FROM maven:3.9.4-eclipse-temurin-21 AS builder
LABEL "language"="java"
LABEL "framework"="spring-boot"
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENV JAVA_OPTS=""
ENV PORT=8080

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT}"]