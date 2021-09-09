FROM openjdk:12-jdk-alpine as build
#FROM adoptopenjdk/openjdk12-openj9:latest as build
WORKDIR /workspace/app


COPY gradlew .
COPY gradle gradle
COPY gradle.properties gradle.properties
COPY version.gradle .
COPY settings.gradle .
COPY build.gradle .
COPY libs libs
RUN ./gradlew dependencies

COPY src src
RUN ./gradlew bootJar -x test
#RUN mkdir ./libs
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

#FROM adoptopenjdk/openjdk12-openj9:latest
FROM openjdk:12-jdk-alpine
RUN apk add --no-cache tzdata
ENV TZ Europe/Brussels
ENV LOGGING_FILE_NAME=""
VOLUME /tmp

ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-XX:MaxRAMPercentage=80.0", "-Djdk.tls.client.protocols=TLSv1.2" ,"-cp","app:app/lib/*","com.ulegalize.UlegalizeDriveApplication"]

