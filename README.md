<h1 align="center">InproDer-Eval</h1>
<div align="center">
    <strong>In</strong>tra- and <strong>In</strong>ter<strong>pro</strong>cedural <strong>Der</strong>ivation Trees <strong>Eval</strong>uation
</div>

---

This project contains source code to evaluate the approach used in the library InproDer and compare it to basic taint-tracking and privacy flow graph generation.

---
## Usage
This project uses [just](http://just.systems) to keep testing and running the evaluation locally while development easy.
You can use the following [just](http://just.systems) recipes:

| Recipe                 | Runs recipes (in order)    | Description                                                                              |
|------------------------|----------------------------|------------------------------------------------------------------------------------------|
| build                  | none                       | Builds jar of evaluation software with all dependencies in target folder                 |
| compile-test           | none                       | Compiles the test HelloWorld java program located in src/test/java/example               |
| start                  | none                       | Runs built jar in target folder                                                          |
| verify                 | none                       | Verifies build using "mvn verify" command.                                               |
| verify-clean           | none                       | Clean verifies build using "mvn verify clean" command.                                   |
| docker-build version   | none                       | Builds a docker image with "version" version handle.                                     |
| docker-run version     | none                       | Runs docker container with "version" version handle.                                     |
| compile-full-run       | compile-test, build, start | Fully builds everything from scratch to run again                                        |
| compile-eval-run       | build, start               | Builds the evaluation software and runs it. It does not recompile the test java program. |

