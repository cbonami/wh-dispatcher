# test messages

webhook:

{
  "url": "http://wh-subscriber-dummy:8081/postit",
  "name": "workingWebhookLocal",
  "pubSub": false
}

curl -X POST "http://localhost:8080/api/webhooks" -H  "accept: application/hal+json" -H  "Content-Type: application/json" -d "{\"url\":\"http://wh-subscriber-dummy:8081/postit\",\"name\":\"workingWebhookLocal\",\"pubSub\":false}"

{ "url":"http://problem.com", "name":"problematic"}

message:

{
  "type": "SomethingHappenedEvent",
  "data": "what the hell happened ?"
}

curl -X POST "http://localhost:8080/api/webhooks/workingWebhookLocal/messages?bucketId=none" -H  "accept: application/hal+json" -H  "Content-Type: application/json" -d "{\"type\":\"SomethingHappenedEvent\",\"data\":\"what the hell happened ?\"}"

# postbin

http://ptsv2.com/t/p10lt-1611758591/post


# post to ws-subscriber-fake
curl --header "Content-Type: application/json" --request POST \
--data '{"username":"xyz","password":"xyz"}' http://localhost:8081/postit

# -------------

> fyi, I am using a docker host running in Ubuntu 20.04 in Linux subsystem (WSL2) for Windows 10

We'll start a redis container and use [port forwarding](https://ibmimedia.com/blog/258/how-to-use-netsh-to-configure-port-forwarding-on-windows) to connect to the docker container.

Run Powershell in Admin mode (or use [sudo for windows](http://blog.lukesampson.com/sudo-for-windows)):

```shell
docker run --name redis -e ALLOW_EMPTY_PASSWORD=yes bitnami/redis:latest

# Delete any existing port 6379 forwarding
sudo netsh interface portproxy delete v4tov4 listenport="6379" 

# Get the private IP of the WSL2 instance
$wslIp=(wsl -d Ubuntu-20.04 -e sh -c "ip addr show eth0 | grep 'inet\b' | awk '{print `$2}' | cut -d/ -f1") 

# Forward host port
# note: listenport is the local port 
sudo netsh interface portproxy add v4tov4 listenport="6379" connectaddress="$wslIp" connectport="6379"
sudo netsh interface portproxy add v4tov4 listenport="6380" connectaddress="$wslIp" connectport="6379"
```

MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
network.proxy.allow_hijacking_localhost  true


C:\Users\cbona\dev\wh-dispatcher\target\gatling


sum(rate(http_server_requests_seconds_sum{application="$application", instance="$instance", status!~"5.."}[1m]))/sum(rate(http_server_requests_seconds_count{application="$application", instance="$instance", status!~"5.."}[1m]))

sum(rate(acerta_webhook_invocation_seconds_sum{application="$application", instance="$instance", status!~"5.."}[1m]))/sum(rate(acerta_webhook_invocation_seconds_count{application="$application", instance="$instance", status!~"5.."}[1m]))

sum(rate(acerta_webhook_invocation_seconds_sum{application="webhook-dispatcher"}[1m]))/sum(rate(acerta_webhook_invocation_seconds_count{application="webhook-dispatcher"}[1m]))

max(acerta_webhook_invocation_seconds_max{application="webhook-dispatcher"})

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
* https://www.novatec-gmbh.de/en/blog/including-hal-browser-spring-boot-without-using-spring-data-rest/
* https://github.com/sahlas/gatling-examples/blob/initial_commit/src/user-files/simulations/players/PlayerCreateScenario.scala
* https://devqa.io/gatling-maven-performance-test-framework/
* https://code.visualstudio.com/docs/java/java-spring-cloud
* https://medium.com/swlh/build-a-docker-image-using-maven-and-spring-boot-58147045a400
* https://www.baeldung.com/jib-dockerizing
* https://dzone.com/articles/using-spring-data-redis-in-spring-boot-with-custom
* https://www.concretepage.com/spring-4/spring-data-redis-example
* https://keepgrowing.in/tools/monitoring-spring-boot-projects-with-prometheus/
* https://www.callicoder.com/spring-boot-actuator-metrics-monitoring-dashboard-prometheus-grafana/
* https://real-world-plantuml.com/
* https://blog.knoldus.com/tired-off-creating-grafana-dashboards-manually-lets-automate-it/
