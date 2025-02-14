#!/usr/bin/env just --justfile

build:
    @echo 'Building main software...'
    JAVA_HOME=/Users/felixkatzenberg/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home/ mvn clean compile assembly:single
    @echo 'Successfully built main software!'

compile-test:
    @echo 'Compiling Test Java program...'
    javac --release 21 -g src/test/java/example/HelloWorld.java
    javac --release 21 -g src/test/java/example/Student.java src/test/java/example/Status.java
    @echo 'Successfully compiled test java program!'

start:
    @echo 'Executing packaged jar...'
    /Users/felixkatzenberg/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home/bin/java -cp target/InproDerEval-1.0-SNAPSHOT-jar-with-dependencies.jar de.felixkat.inprodereval.MainKt -st 0:100 --output temp/
    @echo 'Executed packaged jar!'

verify:
    @echo 'Verifying evaluation program...'
    mvn verify
    @echo 'Verified evaluation program!'

verify-clean:
    @echo 'Clean verifying evaluation program...'
    mvn verify clean
    @echo 'Clean verified evaluation program!'

docker-build version user token:
    @echo 'Building docker image version {{version}}...'
    docker build --build-arg USER_NAME={{user}} --build-arg ACCESS_TOKEN={{token}} -t inproder-eval:{{version}} .
    @echo 'Built docker image!'

docker-run version:
    @echo 'Running docker image version {{version}}...'
    docker run -it --rm inproder-eval:{{version}} -st 0:10
    @echo 'Successfully ran docker image!'

compile-full-run: compile-test build start
compile-eval-run: build start
