#!/bin/sh
redis-cli --raw keys "*:*:*" | xargs redis-cli del
redis-cli keys "*" | xargs -L1 -I '$' echo '"$"' | xargs redis-cli del