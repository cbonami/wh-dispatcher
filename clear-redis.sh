#!/bin/sh
redis-cli -h redis --raw keys "*:*:*" | xargs redis-cli -h redis del
redis-cli -h redis keys "*" | xargs -L1 -I '$' echo '"$"' | xargs redis-cli -h redis del
