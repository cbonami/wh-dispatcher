#!/bin/sh
GF_PATHS_CONFIG="/etc/grafana/grafana.ini"
GF_PATHS_DATA="/var/lib/grafana"
GF_PATHS_HOME="/usr/share/grafana"
GF_PATHS_LOGS="/var/log/grafana"
GF_PATHS_PLUGINS="/var/lib/grafana/plugins"
GF_PATHS_PROVISIONING="/etc/grafana/provisioning"
GF_INSTALL_PLUGINS=grafana-clock-panel,grafana-piechart-panel

if [ ! -z "${GF_INSTALL_PLUGINS}" ]; then
  OLDIFS=$IFS
  IFS=','
  for plugin in ${GF_INSTALL_PLUGINS}; do
    IFS=$OLDIFS
    grafana-cli --pluginsDir "${GF_PATHS_PLUGINS}" plugins install ${plugin}
  done
fi
