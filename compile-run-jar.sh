#!/bin/bash

# Fix for the host.docker.internal bug
# https://github.com/docker/for-linux/issues/264

ping -c1 -q host.docker.internal 2>&1 | grep "bad address" >/dev/null && echo "$(netstat -nr | grep '^0\.0\.0\.0' | awk '{print $2}') host.docker.internal" >> /etc/hosts && echo "Hosts File Entry Added for Linux!!!" || :

lein uberjar

java -jar target/uberjar/g2.jar run migrate || exit
java -jar target/uberjar/g2.jar run
