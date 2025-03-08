<h1 align="center">InproDer-Eval</h1>
<div align="center">
    <strong>In</strong>tra- and <strong>In</strong>ter<strong>pro</strong>cedural <strong>Der</strong>ivation Trees <strong>Eval</strong>uation
</div>

---

## What is it doing?

This project contains source code to evaluate the approach used in the library InproDer and compare the privacy flow graph generation and the derivation tree generation itself.

---
## Docker Usage

Run the docker container for easy evaluation.
The container will run the evaluation software and generate the results.
In order to extract the results and, if some happen, errors from the container, you can use the following command:

```bash
docker run -d -v /path/to/results/on/host:/root/results -v /path/to/errors/on/host:/root/errors --name eval ghcr.io/fexooo/inproder-eval:latest -st 0:10000
```
Change the `/path/to/results/on/host` and `/path/to/errors/on/host` to the desired paths to folders on your host machine.
Using `-st 0:10000` defines how many artifacts will be downloaded and evaluated (in this case 10000 artifacts) from the Maven Central Repository.

---
## Development Usage

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

