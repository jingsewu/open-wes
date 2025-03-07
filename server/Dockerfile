# Build stage (compile all JARs)
FROM gradle:8.3-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :server:station-server:bootJar \
    :server:gateway-server:bootJar \
    :server:wes-server:bootJar -x test

# Stage for station-server
FROM openjdk:17-jdk-slim AS station
COPY --from=build /app/server/station-server/build/libs/station-server*.jar /app/station-server.jar
CMD ["java", "-jar", "/app/station-server.jar"]

# Stage for gateway-server
FROM openjdk:17-jdk-slim AS gateway
COPY --from=build /app/server/gateway-server/build/libs/gateway-server*.jar /app/gateway-server.jar
CMD ["java", "-jar", "/app/gateway-server.jar"]

# Stage for wes-server
FROM openjdk:17-jdk-slim AS wes
COPY --from=build /app/server/wes-server/build/libs/wes-server*.jar /app/wes-server.jar
CMD ["java", "-jar", "/app/wes-server.jar"]
