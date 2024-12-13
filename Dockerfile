FROM openjdk:23-jdk-oracle AS builder

LABEL name="noticeboardapp"

ARG COMPILE_DIR=/compiledir

WORKDIR ${COMPILE_DIR}

COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn
COPY src src

RUN ./mvnw clean package -Dmaven.test.skip=true





FROM openjdk:23-jdk-oracle

ARG WORKDIR=/app

WORKDIR ${WORKDIR}

COPY --from=builder /compiledir/target/noticeboard-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8080
ENV PUBLISHSERVER_URL=https://publishing-production-d35a.up.railway.app

EXPOSE ${SERVER_PORT}

HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
    CMD curl -s -f http://localhost:${SERVER_PORT}/healthz || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]