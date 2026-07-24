# =========================================================
# Stage 1: Build Stage (Maven + OpenJDK 17)
# =========================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# =========================================================
# Stage 2: Runtime Stage (Lightweight JRE 17)
# =========================================================
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# Enterprise Security: Run application as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled executable JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Assign ownership to appuser
RUN chown -R appuser:appgroup /app

# Switch context to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Production JVM flags and configuration defaults
ENV PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx512m"

# Container entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
