FROM amazoncorretto:21.0.4-alpine3.18

WORKDIR /app
COPY ./battlesnake
COPY ./build/libs/battlenskae-0.0.1-SNAPSHOT.jar app.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]