# G2
[![chat mattermost](https://img.shields.io/badge/chat-mattermost-blue.svg)](https://mattermost.zeus.gent/zeus/channels/g2)

The goal of this project is to create a application that can serve as a gateway for people to contribute on projects of an organization.
Further down the road we want to enable contributions more by adding a level of gamification.

## Getting started
### Prerequisites

You will need [Leiningen][1] 2.0 or above installed.
You will also need to install sassc.

### Installing

Clone the repository

Next copy the dev-config and rename:
```
cp dev-config_template.edn dev-config.edn
```
At this moment sqlite is used as database. This requires no further setup. 
Later we will use mysql which will require extra database configuration at that time.

[1]: https://github.com/technomancy/leiningen

### Running

To start a web server for the application, run:

    lein run 


#### Front-end

The project also includes [clojurescript]((https://clojurescript.org/)). A language with the clojure syntax that compiles to javascript for clienside usage in the browser

To start the clojurescript compiler and set it to automaticaly recompile on source code changes run

    lein cljsbuild auto

If you just want to compile the clojurescript once run
    
    lein cljsbuild once
  
During development we have an even better tool. Figwheel will hot load the code in the browser on every change. Start it using
    
    lein figwheel

## Running the tests

TODO

## Deployment

Compiling the application is as simple as

    lein uberjar
    
You can the run it as you would run any other jar. 
Environment variables set in the dev-config.edn files will also have to be set in production. You can do this by providing them as environment variables or by setting them in the production conf file (/env/prod/resources/config.edn)

    java -jar g2.jar

## Built with

TODO

## Contributing

The Issues tab contains a list of known bugs, wanted features or new ideas, check them out if you are looking to contribute in the project. If you still don't know how to start or what to do, don't hesistate to contact the team.

## Versioning

We will use [SemVer](https://semver.org/) as much as possible for versioning.


## Contact

If you have any questions you can reach us on mattermost on the project channel linked above or send a direct message to `flynn`.

## License

This project is licensed under the MIT License - see the LICENSE.md file for details

## Acknowledgments
* Readme structure inspired on https://gist.github.com/PurpleBooth/109311bb0361f32d87a2
