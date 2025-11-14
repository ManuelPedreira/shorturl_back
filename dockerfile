# Build stage, using Alpine image to minimize layers
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
LABEL language="java"
LABEL framework="spring-boot"
WORKDIR /app

# Copy only what is required for the build
COPY pom.xml .
COPY src ./src

# Build the artifact skipping tests
RUN mvn -B -DskipTests package

# Extract jar layers to optimize Docker cache usage
RUN java -Djarmode=layertools -jar target/*.jar extract && \
    mkdir -p dependencies snapshot-dependencies spring-boot-loader application

# Minimal runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy each layer to maximize cache reuse and reduce rebuilds
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/application/ ./

ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:+UseStringDeduplication -XX:+ExitOnOutOfMemoryError -XX:+UseContainerSupport"
ENV PORT=8080

EXPOSE 8080
ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]