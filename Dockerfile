FROM gradle:jdk15 AS builder
COPY /block-drop-server /home/gradle/default

WORKDIR /home/gradle/default
RUN gradle build --no-daemon

FROM openjdk:15.0.2-jdk AS default
COPY --from=builder /home/gradle/default/build/libs/*.jar /block-drop-server.jar

EXPOSE 8080:8080

CMD [ "java", "-jar", "/block-drop-server.jar" ]