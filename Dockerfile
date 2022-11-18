FROM gradle:7.2.0-jdk17 AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle . .

RUN gradle clean shadowJar

FROM openjdk:17-jdk-slim

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/minestom-lobby.jar
copy run/world /app/world/
WORKDIR /app

CMD ["java", "-jar", "/app/minestom-lobby.jar"]