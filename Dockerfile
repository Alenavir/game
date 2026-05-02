FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

ARG SERVICE_NAME

COPY common-proto common-proto
COPY ${SERVICE_NAME} ${SERVICE_NAME}

RUN chmod +x /app/${SERVICE_NAME}/mvnw

WORKDIR /app/common-proto
RUN /app/${SERVICE_NAME}/mvnw install -DskipTests --no-transfer-progress -f pom.xml

WORKDIR /app/${SERVICE_NAME}
RUN ./mvnw package -DskipTests --no-transfer-progress

FROM eclipse-temurin:17-jre
WORKDIR /app
ARG SERVICE_NAME
COPY --from=builder /app/${SERVICE_NAME}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
