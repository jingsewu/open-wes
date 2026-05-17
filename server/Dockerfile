# Runtime-only images.
# JARs are compiled in the GitHub Actions runner before this Dockerfile is built.
# See .github/workflows/deploy.yml build-backend job.

FROM eclipse-temurin:17-jre-jammy AS station
COPY server/station-server/build/libs/station-server*.jar /app/station-server.jar
CMD ["java", "-jar", "/app/station-server.jar"]

FROM eclipse-temurin:17-jre-jammy AS gateway
COPY server/gateway-server/build/libs/gateway-server*.jar /app/gateway-server.jar
CMD ["java", "-jar", "/app/gateway-server.jar"]

FROM eclipse-temurin:17-jre-jammy AS wes
COPY server/wes-server/build/libs/wes-server*.jar /app/wes-server.jar
CMD ["java", "-jar", "/app/wes-server.jar"]
