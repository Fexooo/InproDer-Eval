#!/usr/bin/env just --justfile

build:
    @echo 'Building main software...'
    mvn clean compile assembly:single
    @echo 'Successfully built main software!'

compile-test:
    @echo 'Compiling Test Java program...'
    javac --release 21 -g src/test/java/example/HelloWorld.java
    @echo 'Successfully compiled test java program!'

start:
    @echo 'Executing packaged jar...'
    java -cp target/InproDerEval-1.0-SNAPSHOT-jar-with-dependencies.jar de.felixkat.inprodereval.MainKt
    @echo 'Executed packaged jar!'

verify:
    @echo 'Verifying evaluation program...'
    mvn verify
    @echo 'Verified evaluation program!'

verify-clean:
    @echo 'Clean verifying evaluation program...'
    mvn verify clean
    @echo 'Clean verified evaluation program!'

docker-build inproder-ver version:
    @echo 'Building docker image version {{version}} with InproDer version {{inproder-ver}}...'
    @echo 'Make sure InproDer is currently installed in your maven dependencies!'
    cp ~/.m2/repository/de/felixkat/InproDer/InproDer/{{inproder-ver}}/InproDer-{{inproder-ver}}.jar temp/InproDer.jar
    docker build -t inproder-eval:{{version}} .
    @echo 'Built docker image!'

docker-run version:
    @echo 'Running docker image version {{version}}...'
    docker run -it --rm inproder-eval:{{version}}
    @echo 'Successfully ran docker image!'

compile-full-run: compile-test build start
compile-eval-run: build start
