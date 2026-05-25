FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY server/ .
RUN ./gradlew :modules-simulator:rcs-simulator:bootJar -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/modules-simulator/rcs-simulator/build/libs/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
