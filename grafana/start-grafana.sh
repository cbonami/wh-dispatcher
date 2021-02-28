#!/bin/sh
export GF_PATHS_CONFIG="/etc/grafana/grafana.ini"
export GF_PATHS_DATA="/var/lib/grafana"
export GF_PATHS_HOME="/usr/share/grafana"
export GF_PATHS_LOGS="/var/log/grafana"
export GF_PATHS_PLUGINS="/var/lib/grafana/plugins"
export GF_PATHS_PROVISIONING="/etc/grafana/provisioning"
exec grafana-server --homepath="$GF_PATHS_HOME" --config="$GF_PATHS_CONFIG"
