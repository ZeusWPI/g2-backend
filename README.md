# G2
[![chat mattermost](https://img.shields.io/badge/chat-mattermost-blue.svg)](https://mattermost.zeus.gent/zeus/channels/g2)
The goal of this project is to create a application that can serve as a gateway for people to contribute on projects of an organization.
Further down the road we want to enable contributions more by adding a level of gamification.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.
You will also need to install sassc.

Next copy the dev-config and rename:
```
cp dev-config_template.edn dev-config.edn
```

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

```
lein run    
```

### Front-end

The project also includes [clojurescript]((https://clojurescript.org/)). A language with the clojure syntax that compiles to javascript for clienside usage in the browser

To start the clojurescript compiler and set it to automaticaly recompile on source code changes run

    lein cljsbuild auto

If you just want to compile the clojurescript once run
    
    lein cljsbuild once
  
During development we have an even better tool. Figwheel will hot load the code in the browser on every change. Start it using
    
    lein figwheel

## License

Copyright © 2019 FIXME
