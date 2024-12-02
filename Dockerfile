FROM maven:3.9.9-amazoncorretto-23
LABEL authors="felixkatzenberg"

# Install InproDer manually
ADD ./temp/InproDer.jar /InproDer.jar
RUN mvn install:install-file -Dfile=InproDer.jar -DgroupId=de.felixkat.InproDer -DartifactId=InproDer -Dversion=1.0-SNAPSHOT -Dpackaging=jar

ADD pom.xml /
RUN mvn verify clean
ADD . /
RUN mvn compile assembly:single

FROM openjdk:23-jdk
WORKDIR /root/

COPY --from=0 /target/*-jar-with-dependencies.jar app.jar
ADD src/test src/test
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","./app.jar"]