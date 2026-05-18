FROM eclipse-temurin:26-jdk AS build
WORKDIR /workspace


COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true


COPY src src
RUN ./gradlew --no-daemon bootJar -x test


FROM eclipse-temurin:26-jre
WORKDIR /app


RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=build /workspace/build/libs/*.jar app.jar
RUN chown spring:spring app.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxRAMPercentage=75", \
  "-jar", "/app/app.jar"]