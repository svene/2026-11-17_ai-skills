# @ARTIFACT_ID@

A Quarkus multi-module project. The runnable application is the `app-server` module.

## Build and run

Pick up the pinned JDK with `sdk env` (see `.sdkmanrc`), then:

```sh
./mvnw verify                                       # build and run all tests
./mvnw quarkus:dev -pl app-server -am               # dev mode with live reload
java -jar app-server/target/quarkus-app/quarkus-run.jar   # run the packaged app
```
