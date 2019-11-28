(defproject g2 "0.1.0-SNAPSHOT"
  :description "G2"
  :url "https://github.com/zeuswpi/g2"
  :dependencies [[buddy "2.0.0"] ; auth
                 [cheshire "5.8.1"] ; Clojure JSON and JSON SMILE (binary json format) encoding/decoding
                 [clojure.java-time "0.3.2"]
                 [com.cognitect/transit-clj "0.8.319"]
                 [compojure "1.6.1"]
                 [conman "0.8.3"]
                 [cprop "0.1.13"]
                 [funcool/struct "1.3.0"]
                 [luminus-immutant "0.2.5"]
                 [luminus-migrations "0.6.4"]
                 [luminus-transit "0.1.1"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [metosin/muuntaja "0.6.4"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.cli "0.4.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [mysql/mysql-connector-java "8.0.12"]
                 [com.google.protobuf/protobuf-java "3.6.1"] ; support for google protobuff format
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring-cors "0.1.13"]
                 [selmer "1.12.6"] ; html template renderer
                 [slingshot "0.12.2"]
                 [clj-http "3.9.1"] ; http request tool
                 ;                 [org.apache.logging.log4j/log4j-api "2.11.0"]
                 ;                 [org.apache.logging.log4j/log4j-core "2.11.0"]
                 ;                 [org.apache.logging.log4j/log4j-1.2-api "2.11.0"]
                 [metosin/reitit "0.3.7"]]


  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot g2.core

  :plugins [[lein-immutant "2.1.0"]
            [lein-auto "0.1.2"]
            [lein-cloverage "1.1.1"]]

  :profiles
  {:uberjar       {:omit-source    true
                   :aot            :all
                   :uberjar-name   "g2.jar"
                   :source-paths   ["env/prod/clj"]
                   :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev]
   :test          [:project/dev :project/test]

   :project/dev   {:jvm-opts       ["-Dconf=dev-config.edn"]
                   :dependencies   [[expound "0.7.2"] ; Clearer error messages
                                    [pjstadig/humane-test-output "0.9.0"] ; Test output is pretty printed. && Equality assertions are also diffed.
                                    [ring/ring-mock "0.3.2"] ; Library to create mock Ring requests for unit tests
                                    [ring/ring-devel "1.7.1"]
                                    [prone "1.6.1"]] ; Better exception reporting middleware for Ring.
                   :plugins        [[com.jakemccrary/lein-test-refresh "0.23.0"]]
                   :source-paths   ["env/dev/clj"]
                   :resource-paths ["env/dev/resources"]
                   :repl-options   {:init-ns user}
                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]}
   :project/test  {:jvm-opts       ["-Dconf=test-config.edn"]
                   :resource-paths ["env/test/resources"]}}

  :repl-options {;; If nREPL takes too long to load it may timeout,
                 ;; increase this to wait longer before timing out.
                 ;; Default to 30000 (30 seconds)
                 :timeout 200000})
