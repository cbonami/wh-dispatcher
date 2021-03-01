#!/bin/sh
GF_PATHS_CONFIG="/etc/grafana/grafana.ini"
GF_PATHS_DATA="/var/lib/grafana"
GF_PATHS_HOME="/usr/share/grafana"
GF_PATHS_LOGS="/var/log/grafana"
GF_PATHS_PLUGINS="/var/lib/grafana/plugins"
GF_PATHS_PROVISIONING="/etc/grafana/provisioning"
GF_INSTALL_PLUGINS=grafana-clock-panel,grafana-piechart-panel

exec grafana-server --homepath="$GF_PATHS_HOME" --config="$GF_PATHS_CONFIG" \
  cfg:default.log.mode="console"                            \
  cfg:default.paths.data="$GF_PATHS_DATA"                   \
  cfg:default.paths.logs="$GF_PATHS_LOGS"                   \
  cfg:default.paths.plugins="$GF_PATHS_PLUGINS"             \
  cfg:default.paths.provisioning="$GF_PATHS_PROVISIONING"
