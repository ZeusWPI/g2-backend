#!/bin/bash

# Fix for the host.docker.internal bug
# https://github.com/docker/for-linux/issues/264

echo "$(netstat -nr | grep '^0\.0\.0\.0' | awk '{print $2}') host.docker.internal" >> /etc/hosts && echo "Hosts File Entry Added for Linux!!!" || :
