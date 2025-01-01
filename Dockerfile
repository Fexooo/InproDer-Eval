FROM maven:3.9.9-amazoncorretto-23
LABEL authors="felixkatzenberg"

ADD .github/workflows/maven-settings.xml /
ADD pom.xml /
ARG USER_NAME
ARG ACCESS_TOKEN
RUN mvn -s maven-settings.xml verify clean -Denv.user=${USER_NAME} -Denv.accesstoken=${ACCESS_TOKEN}
ADD . /
RUN mvn -s maven-settings.xml compile assembly:single -Denv.user=${USER_NAME} -Denv.accesstoken=${ACCESS_TOKEN}
RUN rm -f maven-settings.xml

FROM openjdk:23-jdk
WORKDIR /root/

COPY --from=0 /target/*-jar-with-dependencies.jar app.jar
ADD src/test src/test
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","./app.jar"]