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

