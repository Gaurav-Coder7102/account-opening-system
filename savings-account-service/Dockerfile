# Multi-stage Dockerfile for savings-account-service
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copy Maven reactor configuration and all modules
COPY pom.xml .
COPY common-lib/ common-lib/
COPY authentication-service/ authentication-service/
COPY customer-service/ customer-service/
COPY account-opening-service/ account-opening-service/
COPY savings-account-service/ savings-account-service/

# Build savings-account-service along with required dependencies (common-lib)
RUN mvn -pl savings-account-service -am clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create and switch to non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /workspace/savings-account-service/target/*.jar app.jar

EXPOSE 8084

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
