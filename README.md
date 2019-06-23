# G2
[![chat mattermost](https://img.shields.io/badge/chat-mattermost-blue.svg)](https://mattermost.zeus.gent/zeus/channels/g2)

The goal of this project is to create a application that can serve as a gateway for people to contribute on projects of an organization. We want to improve the visibility of subparts of projects to make it more accessible to contribute.

Further down the road we want to add a level of gamification to the application, inspired by [Gamification](https://zeus.ugent.be/game)

## Getting started
### Prerequisites

**[Leiningen][1]** 2.0 or above.\
Leiningen is a development tool that mainly manages the dependencies and build configuration of the project.

**Mariadb or MySQL** \
The database used by the backend will be primarily mariadb.

### Installing

* Clone the repository
* Copy the template configuration files for development and testing:
```
cp dev-config_template.edn dev-config.edn
cp test-config_template.edn test-config.edn
```
* Check these configs for variables that are different in your setup. They should normally work out of the box.

* Create a new local database and create or modify a database user so it has access to the database. (You can use the dev database for the tests to, but it will possibly be filled with testing data.)
* Update the database-url parameter in the `dev-config.edn` and `test-config.edn` file with your newly created db and user.

[1]: https://github.com/technomancy/leiningen

### Running

To start a web server for the application, first run the migrations, then start the server:

    lein run migrate
    lein run 

While actively developing the application you will change files and therefore namespaces. When using the above method you will need to restart the webserver everytime you want to see the new changes. This is quite a slow process and therefor not recommended. Instead use the following workflow.

* Start a [repl](https://clojure.org/guides/repl/introduction) using

      lein repl
  Here you can execute arbitrary clojure code. The [file with the user namespace](https://github.com/ZeusWPI/g2/blob/master/env/dev/clj/user.clj) will be automaticaly loaded into the repl and it's functions will be available in the repl.
    
* Now start the server and then run the migrations.

      (start)
      (migrate)
      
* Now browse to `localhost:3000` to see the webserver. 
  At this moment you will land onto a page with some testing links. This is going to be removed later on when the [frontend](https://github.com/zeuswpi/g2-frontend) has more functionality.
  
  We use swagger to serve a nice visual and handy frontend with out api. This enables the developer to quickly discover all the needed andpoints and get their specification in the process.
  Surf to `localhost:3000/api-docs/` to find the documentation.
 
* If you want to see some data quicly, try out the `/repository/sync` path. It synchronises the repositories of the organization configured in the `dev-config` file with the g2 backend. You can then request a list of these repo's on `/repository`
  
#### Loading code changes

Changed clojure files will not be automatically loaded into the webserver. You can however load your changed file into the repl with 1 easy command.

    (use 'g2.my_changed_file :reload)
    
The webserver will detect the change and will quickly restart.

#### Using git hooks

WORK IN PROGRESS

Running the application on localhost makes it impossible for github hooks to find it. You have a few applications that can help you with this like ... but you can also do this yourself using simple ssh port forwarding. You need a server with a public ip adres and ssh access.

Using following command all requests to your servers ip on port 9123 will be tunneled to your computer on port 3000. In this way you can register hooks on my.server.ip:9123.

    ssh -R 9123:localhost:3000 my.server.ip

## Running the tests

To run tests run the next command. This will autodetect changes in the backend files and they will rerun themselves automatically.

	lein test-refresh
	
If you want to run them only once run

	lein test


## Deployment

Compiling the application is as simple as

    lein uberjar
    
You can the run it as you would run any other jar. 
Environment variables set in the dev-config.edn file will also have to be set in production. You can do this by providing them as environment variables or by setting them in the production conf file (/env/prod/resources/config.edn)

    java -jar g2.jar

## Built with

* Clojure
* Leiningen
* Luminus (and their whole stack)

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
* The amazing Luminus template with a solid 
