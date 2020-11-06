FROM clojure:openjdk-11-lein-2.9.1-slim-buster

# Update the system and install netstat command
RUN apt-get update
RUN apt-get install -y net-tools
RUN apt-get install -y netcat

# download the dependencies and compile the project ahead of time. This will significantly reduce startup time when you run your image
RUN mkdir -p /g2
WORKDIR /g2
COPY project.clj /g2/
RUN lein deps

# Now copy the rest of the project over
COPY src /g2/src
COPY resources /g2/resources
COPY env /g2/env
COPY dev-config_template.edn /g2/dev-config.edn

RUN lein uberjar

# Copy stuff that is not needed for compilation
COPY scripts /g2/scripts

EXPOSE 3000

COPY *entrypoint.sh /g2/
