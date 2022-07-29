FROM openjdk:11-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} microstreaming-analytics.jar
ENTRYPOINT ["java","-jar","/microstreaming-analytics.jar"]
