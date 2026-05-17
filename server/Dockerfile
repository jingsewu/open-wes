# Build stage (compile all JARs)
FROM gradle:8.3-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :server:station-server:bootJar \
    :server:gateway-server:bootJar \
    :server:wes-server:bootJar -x test

# Stage for station-server
FROM eclipse-temurin:17-jre-jammy AS station
COPY --from=build /app/server/station-server/build/libs/station-server*.jar /app/station-server.jar
CMD ["java", "-jar", "/app/station-server.jar"]

# Stage for gateway-server
FROM eclipse-temurin:17-jre-jammy AS gateway
COPY --from=build /app/server/gateway-server/build/libs/gateway-server*.jar /app/gateway-server.jar
CMD ["java", "-jar", "/app/gateway-server.jar"]

# Stage for wes-server
FROM eclipse-temurin:17-jre-jammy AS wes
COPY --from=build /app/server/wes-server/build/libs/wes-server*.jar /app/wes-server.jar
CMD ["java", "-jar", "/app/wes-server.jar"]
