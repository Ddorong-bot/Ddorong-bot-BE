# 1단계: Gradle 빌드 (JDK 21 사용)
FROM gradle:8.4.0-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test

# 2단계: 실행용 JDK 21 이미지 (더 작고 빠름)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]