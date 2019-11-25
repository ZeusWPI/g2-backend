FROM clojure:lein-alpine

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

EXPOSE 3000

# For some reasing CMD doesn't work here so it's specified in the docker-compose file. If used here together with docker-compose it enters the repl as specified in the clojure image and exists immediatly after the repl is started.
# CMD lein run migrate && lein run

COPY docker-entrypoint.sh /g2/
RUN chmod +x /g2/docker-entrypoint.sh
