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

compile-full-run: compile-test build start
compile-eval-run: build start
