# Movie Battle App

### Components
* Basic web application
* Data analyzer
* Data collector

### Technology stack

This codebase is written in a language called [Kotlin](https://kotlinlang.org) that is able to run on the JVM with full
Java compatibility.
It uses the [Ktor](https://ktor.io) web framework, and runs on the [Netty](https://netty.io/) web server.
HTML templates are written using [Freemarker](https://freemarker.apache.org).
The codebase is tested with [JUnit](https://junit.org/) and uses [Gradle](https://gradle.org) to build a jarfile.

## Run locally

1.  Start postgress and rabbit on docker (required to run the apps and the tests)
    ```bash
    docker compose up
    ```
1. Run flyway migrations on dev and test db's
    ```bash
    ./gradlew devMigrate
    ./gradlew testMigrate
    ```

1.  Build and test the apps
    ```bash
    ./gradlew clean build
    ```

1.  Run the servers locally using the below examples.

    Web server

    ```bash
    ./gradlew a:b:r
    ```

    Data collector

    ```bash
    ./gradlew a:d-c:r
    ```

    Data analyzer
    
    ```bash
    ./gradlew a:d-a:r
    ```
1. Open the app at http://localhost:8888

1. Open in browser http://localhost:8888/collect-movies to populate the db with initial data 