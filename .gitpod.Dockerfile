FROM gitpod/workspace-full

USER gitpod

# prometheus - https://medium.com/devops-dudes/install-prometheus-on-ubuntu-18-04-a51602c6256b
RUN wget https://github.com/prometheus/prometheus/releases/download/v2.25.0/prometheus-2.25.0.linux-amd64.tar.gz \
 && tar -xf prometheus-2.25.0.linux-amd64.tar.gz \
 && sudo mv prometheus-2.25.0.linux-amd64/prometheus prometheus-2.25.0.linux-amd64/promtool /usr/local/bin \
 && sudo mkdir /etc/prometheus /var/lib/prometheus \
 && sudo mv prometheus-2.25.0.linux-amd64/consoles prometheus-2.25.0.linux-amd64/console_libraries /etc/prometheus \
 && rm -r prometheus-2.25.0.linux-amd64*

# grafana
RUN curl https://packages.grafana.com/gpg.key | sudo apt-key add - \
 && sudo add-apt-repository "deb https://packages.grafana.com/oss/deb stable main" \
 && sudo apt-get -y install grafana

# redis
RUN sudo apt-get update \
 && sudo apt-get install -y redis-server \
 && sudo apt-get -y install --no-install-recommends redis-tools \
 && sudo rm -rf /var/lib/apt/lists/*
