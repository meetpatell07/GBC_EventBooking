# Build Stage
#----------
FROM openjdk:23-jdk-slim AS builder

COPY . /home/gradle/src

WORKDIR /home/gradle/src

RUN chmod +x gradlew


RUN ./gradlew build -x test

#---------------
# Package
#---------------
FROM openjdk:23-jdk-slim

RUN mkdir /app

COPY --from=builder /home/gradle/src/build/libs/*jar /app/api-gateway.jar

ENV POSTGRES_USERNAME=admin \
    POSTGRES_PWD=password

EXPOSE 9000

ENTRYPOINT ["java","-jar", "/app/api-gateway.jar"]