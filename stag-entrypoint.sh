#!/bin/sh

# Add docker host address to the hosts file
./scripts/add-docker-host-to-hosts-file.sh

./scripts/wait_for_service db 3306

echo "Migration with the following database"
echo "$DATABASE_URL"

# migrate and run the application
java -Dconf=dev-config.edn -jar target/uberjar/g2.jar migrate || exit 1
echo "Migration done."
echo "Starting server"
java -Dconf=dev-config.edn -jar target/uberjar/g2.jar
