FROM eclipse-temurin:21-jdk-alpine AS build
ENV HOME=/usr/app
WORKDIR $HOME
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
ARG JAR_FILE=/usr/app/target/*.jar
COPY --from=build $JAR_FILE ./runner.jar
RUN chown spring:spring runner.jar
USER spring
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "runner.jar"]
