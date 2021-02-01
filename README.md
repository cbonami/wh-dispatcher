# Webhook Dispatcher

Spring Boot application that 
- offers to event-subscribers an HTTP-API where they can register a webhook (i.e. an endpoint URL in the subscriber's api)
- POSTs event-payloads to these hooks as events occur at publisher's end

## Setup vscode Dev Container

> Note: only works with Visual Studio Code   

Development environment is 100% pre-cooked and standard for all developers. Docker-compose is used to spin up dependencies like redis etc. The development env/container is part of the docker-compose definition, and shares the same container network with the other containers (redis etc). Ports that are opened (by the webapp) are automatically forwarded to the host machine (probably W10).

See [WSL2_DEV_ENV.md](./WSL2_DEV_ENV.md) for instructions.

## Configure Docker Hub Registry

When maven builds the image, JIB will push it to docker hub registry. Credentials need to be provided in [/home/vscode/.m2/settings.xml](/home/vscode/.m2/settings.xml):

```xml
<settings>

  <servers>
    <server>
      <id>registry.hub.docker.com</id>
      <username>cbonami</username>
      <password>....</password>
    </server>
  </servers>

</settings>
```

## Build app and push image

```bash
./mvnw compile jib:build
```

## Run app

```bash
./mvnw spring-boot:run
```

* [http://localhost:8080/browser/browser.html](http://localhost:8080/browser/browser.html) -> [HAL Explorer](https://github.com/toedter/hal-explorer); use this to TRAVERSE the api
* [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) -> Swagger Docs UI
* [http://localhost:8080/v3/api-docs/](http://localhost:8080/v3/api-docs/) -> Open API 3.0 JSON description.
* [http://localhost:8080/actuator](http://localhost:8080/actuator) -> Actuator API to inspect and manage the webservice

## Use app

Perform some HTTP-request via curl, postman, etc. Lazy people simply use the [HAL Explorer](http://localhost:8080/browser/browser.html) or the [Swagger UI](). 

## Load test

```bash
./mvnw gatling:test -Dsimulation=LoadTest -Dduration=3600
```

## Useful commands

```
# empty redis db
redis-cli FLUSHDB
# when FLUSHDB disabled (https://stackoverflow.com/questions/59111007/redis-err-unknown-command-flushdb), then use:
redis-cli --raw keys "*:*:*" | xargs redis-cli del
redis-cli keys "*" | xargs -L1 -I '$' echo '"$"' | xargs redis-cli del

# list all keys
redis-cli keys "*"
```


