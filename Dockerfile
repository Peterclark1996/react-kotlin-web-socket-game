FROM node:13.12.0-alpine AS nodeBuilder
COPY /block-drop-client /app

WORKDIR /app
RUN npm install
RUN npm run build

FROM gradle:jdk15 AS kotlinBuilder
COPY /block-drop-server /home/gradle/default

WORKDIR /home/gradle/default
RUN gradle build --no-daemon

FROM openjdk:15.0.2-jdk AS default
COPY --from=kotlinBuilder /home/gradle/default/build/libs/*.jar /block-drop-server.jar
RUN mkdir client
COPY --from=nodeBuilder /app/build/* /client/

EXPOSE 8080:8080

CMD [ "java", "-jar", "/block-drop-server.jar" ]