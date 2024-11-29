#!/usr/bin/env just --justfile

build:
    @echo 'Building main software...'
    mvn package
    @echo 'Successfully built main software!'

compile-test:
    @echo 'Compiling Test Java program...'
    javac --release 21 -g src/test/java/example/HelloWorld.java
    @echo 'Successfully compiled test java program!'

start:
    @echo 'Executing packaged jar...'
    java -cp target/TestingSootUp-1.0-SNAPSHOT.jar de.felixkat.TestingSootUp.MainKt
    @echo 'Executed packaged jar!'

run: compile-test build start