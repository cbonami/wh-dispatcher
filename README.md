# Webhook Dispatcher

Service that offers an API to registers webhooks at subscriber's end, and then invokes these hooks (POST) as events
occurs.

## Use

### Start Redis

By default, Redis runs on port 6379.

#### Option: Redis on Docker

> fyi, I am using a docker host running in Ubuntu 20.04 in Linux subsystem (WSL2) for Windows 10

```bash
docker run --name redis -e ALLOW_EMPTY_PASSWORD=yes bitnami/redis:latest
```

Use [port forwarding](https://ibmimedia.com/blog/258/how-to-use-netsh-to-configure-port-forwarding-on-windows) to connect to the docker container.
Run elevated Powershell:

```shell
# Delete any existing port 6379 forwarding
netsh interface portproxy delete v4tov4 listenport="6379" 

# Get the private IP of the WSL2 instance
$wslIp=(wsl -d Ubuntu -e sh -c "ip addr show eth0 | grep 'inet\b' | awk '{print `$2}' | cut -d/ -f1") 

# Forward port
# note: listenport is the local port 
netsh interface portproxy add v4tov4 listenport="6379" connectaddress="$wslIp" connectport="6379"
```

#### Option: Redis on Windows

Just run Redis as a windows service. See instructions. https://medium.com/@binary10111010/redis-cli-installation-on-windows-684fb6b6ac6b

## Run app

API description becomes available at [http://localhost:8080/v3/api-docs/](http://localhost:8080/v3/api-docs/)

## Use app

See [here](src/test/resources/test.http)

Note: the registration service has been developed so that you can register using a simple registration form.

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
