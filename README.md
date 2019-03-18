# jmessenger

[GitHub](https://www.github.com/remicmacs/jmessenger)

## You need it

* Java 1.8
* JavaFX

Beware OpenJDK users : JavaFX is not included in OpenJDK. Install OpenJDK AND OpenJFX.

## Build it

The Gradle wrapper provided by IntelliJ takes care of all Gradle-related things.

Client :

```bash
./gradlew run clientUberJar
```

Server :

```bash
./gradlew run serverUberJar
```

## Run it

First the server :

```bash
java -jar build/libs/jmgr-1.0-SNAPSHOT-srv.jar
```

then a client

```bash
java -jar build/libs/jmgr-1.0-SNAPSHOT-client.jar
```

## API documentation

You can found the Javadoc-generated documentation [here](./docs/javadoc/index.html).
