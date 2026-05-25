#!/bin/bash

APP_HOME=$(cd "$(dirname "$0")/.." && pwd)

MAIN_CLASS="game.gateway.GatewayApplication"

nohup java -cp "$APP_HOME/config:$APP_HOME/lib/*" $MAIN_CLASS \
  > "$APP_HOME/logs/app.log" 2>&1 &

echo "started"