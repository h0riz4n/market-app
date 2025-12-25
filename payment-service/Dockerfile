FROM eclipse-temurin:21-jdk

WORKDIR /app
ENV TZ=Europe/Moscow
COPY ./build/libs/*-SNAPSHOT.jar backend.jar

ENTRYPOINT ["java", "-jar",  "-Dspring.profiles.active=${SPRING_PROFILE}", "backend.jar"]