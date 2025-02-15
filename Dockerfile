FROM maven:3.9.9-amazoncorretto-21
LABEL authors="felixkatzenberg"

RUN mkdir build
WORKDIR /build
ADD dependencies/marin-1.0.0-SNAPSHOT.jar /build/
ADD dependencies/marin-pom.xml /build/
RUN mvn install:install-file \
           -Dfile=marin-1.0.0-SNAPSHOT.jar \
           -DgroupId=org.tudo.sse \
           -DartifactId=marin \
           -Dversion=1.0.0-SNAPSHOT \
           -Dpackaging=jar \
           -DpomFile=marin-pom.xml
RUN rm -f marin-1.0.0-SNAPSHOT.jar
ADD .github/workflows/maven-settings.xml /build/
ADD . /build/
ARG USER_NAME
ARG ACCESS_TOKEN
RUN mvn -s maven-settings.xml verify clean -Denv.user=${USER_NAME} -Denv.accesstoken=${ACCESS_TOKEN}
RUN mvn -s maven-settings.xml clean package -Denv.user=${USER_NAME} -Denv.accesstoken=${ACCESS_TOKEN}
RUN rm -f maven-settings.xml
RUN rm -rf marin

FROM openjdk:21-jdk
WORKDIR /root/
RUN mkdir temp
RUN mkdir results

COPY --from=0 /build/target/*-jar-with-dependencies.jar app.jar
ADD src/test src/test
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","./app.jar","--output","temp/"]