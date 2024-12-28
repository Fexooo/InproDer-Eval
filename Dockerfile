FROM maven:3.9.9-amazoncorretto-23
LABEL authors="felixkatzenberg"

ADD pom.xml /
RUN mvn verify clean
ADD . /
RUN mvn compile assembly:single

FROM openjdk:23-jdk
WORKDIR /root/

COPY --from=0 /target/*-jar-with-dependencies.jar app.jar
ADD src/test src/test
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","./app.jar"]