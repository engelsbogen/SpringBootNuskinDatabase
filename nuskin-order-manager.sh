#!/bin/bash

destRoot=/home/adc/nuskin-order-manager

echo Invoked with parm $1

case "$1" in
  start)
  start-stop-daemon --chdir $destRoot --start --background --quiet -m --pidfile $destRoot/nuskin-order-manager.pid --exec /usr/bin/java -- -jar $destRoot/nuskin-order-manager.jar
  echo Error code: $?
  ;;

  stop)
  echo Stopping
    start-stop-daemon --stop --pidfile $destRoot/nuskin-order-manager.pid
  ;;

  *)
  echo Unknown action
  ;;
esac

exit 0

