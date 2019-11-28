
# Add docker host address to the hosts file
./add-docker-host-to-hosts-file.sh

# migrate and run the application
java -Dconf=dev-config.edn -jar target/uberjar/g2.jar migrate && \
java -Dconf=dev-config.edn -jar target/uberjar/g2.jar

