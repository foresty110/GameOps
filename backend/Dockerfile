# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew --version --no-daemon || true
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --create-home --shell /bin/bash spring
USER spring
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
