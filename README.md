# Webhook Dispatcher

Service that offers an API to registers webhooks at subscriber's end, and then invokes these hooks (POST) as events
occurs.

## Use

### Start Redis

By default, Redis runs on port 6379.

#### Option 1: using WSL2

= preferred option, especially when using WSL-Remote for Containers; development environment is 100% pre-cooked and standard

See [WSL2_DEV_ENV.md](./WSL2_DEV_ENV.md) for setup.

#### Option 2: 100% Windows 10

Just run Redis as a windows service on your local W10 machine. See instructions. https://medium.com/@binary10111010/redis-cli-installation-on-windows-684fb6b6ac6b

## Run app

```bash
./mvnw spring-boot:run
```

* [http://localhost:8080/] -> HAL Browser
* [http://localhost:8080/v3/api-docs/](http://localhost:8080/v3/api-docs/) -> API description.

## Use app

Perform some HTTP-request via curl, or use the examples in the [.http](src/test/resources/test.http)-files. Both IntelliJ and vscode (via an extension) can interprete these standard .http files.  

### Delete application by ID

```bash
curl -v --request DELETE http://localhost:8080/applications/1
```

### Send (POST) a message to an application

```bash
curl -v --header "Content-type: text/plain" --request POST --data "WEBHOOK TEST" http://localhost:8080/applications/1/message
```

## Useful commands

```
# empty redis db
redis-cli FLUSHDB

# 
redis-cli keys *
```

## Useful Resources

* https://spring.io/blog/2020/01/27/creating-docker-images-with-spring-boot-2-3-0-m1
* https://www.baeldung.com/spring-rest-openapi-documentation
* https://www.baeldung.com/spring-data-redis-tutorial
* https://github.com/eugenp/tutorials/tree/master/persistence-modules/spring-data-redis
* https://docs.docker.com/docker-for-windows/wsl/
* https://docs.microsoft.com/en-us/windows/wsl/compare-versions
* [Redis CLI](https://github.com/MicrosoftArchive/redis/releases)
* https://redis.io/topics/indexes
* https://github.com/spring-projects/spring-data-examples/tree/master/redis/repositories
* https://github.com/spring-projects/spring-data-redis/blob/master/src/main/asciidoc/reference/query-by-example.adoc
* https://stackoverflow.com/questions/45419196/query-nested-objects-in-redis-using-spring-data
* https://grokonez.com/spring-framework/spring-boot/spring-data-redis-messaging-pubsub-spring-boot-spring-data-redis-example
* [sudo for windows](http://blog.lukesampson.com/sudo-for-windows)
* https://github.com/docker/for-win/issues/6610
* https://code.visualstudio.com/docs/remote/containers#_quick-start-try-a-dev-container
* https://github.com/bitnami/bitnami-docker-redis
* https://derkoe.dev/blog/development-environment-in-wsl2/
* https://partlycloudy.blog/2020/06/05/wsl2-making-windows-10-the-perfect-dev-machine/
* https://kb.objectrocket.com/redis/run-redis-with-docker-compose-1055
