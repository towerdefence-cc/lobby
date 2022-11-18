FROM eclipse-temurin:17-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/minestom-lobby.jar
COPY run/world /app/world/

CMD ["java", "-jar", "/app/minestom-lobby.jar"]