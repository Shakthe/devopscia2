# Runtime image only: the JAR is compiled, tested and packaged by Maven earlier in the pipeline.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG BUILD_SHA=local
ENV BUILD_SHA=${BUILD_SHA}
RUN addgroup -S app && adduser -S app -G app
COPY target/app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
