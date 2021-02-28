#!/bin/sh
redis-cli -h localhost --raw keys "*:*:*" | xargs redis-cli -h localhost del
redis-cli -h localhost keys "*" | xargs -L1 -I '$' echo '"$"' | xargs redis-cli -h localhost del
