FROM amazoncorretto:23-alpine
WORKDIR /app
COPY target/cex-broker-1.0-SNAPSHOT-jar-with-dependencies.jar cex-broker.jar
ENTRYPOINT ["java", "-jar", "cex-broker.jar"]