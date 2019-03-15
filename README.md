# g2

generated using Luminus version "3.10.40" with the options [sqlite, cljs, auth]

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
