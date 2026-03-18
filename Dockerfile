# ─────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────
FROM gradle:jdk24 AS build

WORKDIR /app

# Cache Gradle wrapper and dependencies separately
COPY gradlew gradlew.bat* ./
COPY gradle/ gradle/
RUN chmod +x gradlew

COPY build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon || true

COPY src/ src/
RUN ./gradlew build -x test --no-daemon

# ─────────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:24-jre-alpine AS runtime

# Non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup -u 1000

WORKDIR /app

COPY --from=build /app/build/libs/flooding-server-v2-*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
