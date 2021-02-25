FROM gitpod/workspace-full

# redis
RUN sudo apt-get update \
 && sudo apt-get install -y redis-server \
 && sudo rm -rf /var/lib/apt/lists/*