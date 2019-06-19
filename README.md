# G2
[![chat mattermost](https://img.shields.io/badge/chat-mattermost-blue.svg)](https://mattermost.zeus.gent/zeus/channels/g2)

The goal of this project is to create a application that can serve as a gateway for people to contribute on projects of an organization.
Further down the road we want to enable contributions more by adding a level of gamification.

## Getting started
### Prerequisites

You will need [Leiningen][1] 2.0 or above installed.\
Leiningen is a development tool that mainly manages the dependencies and build configuration of the project.

You will also need to install sassc.

### Installing

Clone the repository

Next copy the dev-config and rename:
```
cp dev-config_template.edn dev-config.edn
```


[1]: https://github.com/technomancy/leiningen

### Running

Start by running the migrations on the database. Make sure the correct url is set in your config. Migrations can be added overtime, don't forget to execute them!

    lein run migrate

To start a web server for the application, run:

    lein run 
    
You can also start a repl environment which allows for more dynamic and involved programming. Following commands puts the webserver in the same state as the previous commands one but it leaves you in a repl.

    lein repl
    >> (start)
    >> (migrate)

#### Back-end

If you change files and want to see your changes it will go faster in a repl. When using `lein run`, you need to stop the process and restart it. In the repl you can reload a namespace and the server will automatically reload the file into its process. 

    >> (use g2.filename :reload)
    
This reload is not perfect, old namespaces are necessarily removed which can result in a conflict when you change a file with another one but give them the same name.

TODO Add clojure.tools.refresh explanation.


    
If you change SQL queries, restart the database using

    >> (restart-db)


#### Front-end

The project also includes [clojurescript]((https://clojurescript.org/)). A language with the clojure syntax that compiles to javascript for clienside usage in the browser.

We are currently not really serving a frontend but there is a route on `/` for debug purposes. If you want to run clojurescript on there, follow the next instructions.


To start the clojurescript compiler and set it to automaticaly recompile on source code changes run

    lein cljsbuild auto

If you just want to compile the clojurescript once run
    
    lein cljsbuild once
  
During development we have an even better tool. Figwheel will hot load the code in the browser on every change. Start it using
    
    lein figwheel

#### Using git hooks

Running the application on localhost makes it impossible for github hooks to find it. You have a few applications that can help you with this like ... but you can also do this yourself using simple ssh port forwarding. You need a server with a public ip adres and ssh access.

Using following command all requests to your servers ip on port 9123 will be tunneled to your computer on port 3000. In this way you can register hooks on my.server.ip:9123.

    ssh -R 9123:localhost:3000 my.server.ip

## Running the tests

To run tests run the next command. They will autodetect changes and rerun themselves automatically.

First copy the file test-config_example.edn to test-config.edn. Adapt all needed configs for your machine.

Now run the automated tests

	lein test-refresh
    
You can also run the tests once using

    lein test


## Deployment

Compiling the application is as simple as

    lein uberjar
    
You can the run it as you would run any other jar. 
Environment variables set in the dev-config.edn file will also have to be set in production. You can do this by providing them as environment variables or by setting them in the production conf file (/env/prod/resources/config.edn)

    java -jar g2.jar

## Built with

TODO

## Contributing

The Issues tab contains a list of known bugs, wanted features or new ideas, check them out if you are looking to contribute in the project. If you still don't know how to start or what to do, don't hesitate to contact the team.

## Versioning

We will use [SemVer](https://semver.org/) as much as possible for versioning.


## Contact

If you have any questions you can reach us on mattermost on the project channel linked above or send a direct message to `flynn`.

## License

This project is licensed under the MIT License - see the LICENSE.md file for details

## Acknowledgments
* Readme structure inspired on https://gist.github.com/PurpleBooth/109311bb0361f32d87a2
