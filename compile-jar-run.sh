
# Add docker host address to the hosts file
./add-docker-host-to-hosts-file.sh

# migrate and run the application
java -jar target/uberjar/g2.jar run migrate
java -jar target/uberjar/g2.jar run
