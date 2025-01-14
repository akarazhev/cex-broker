FROM amazoncorretto:23-alpine
WORKDIR /app
COPY target/cex-broker-0.1-SNAPSHOT-jar-with-dependencies.jar cex-broker.jar
ENTRYPOINT ["java", "-jar", "cex-broker.jar"]