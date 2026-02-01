# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Gradle wrapper + gradle 설정 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# 소스 복사
COPY src src

# Spring Boot jar 빌드 (테스트 스킵)
RUN ./gradlew clean bootJar -x test

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드된 jar 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
