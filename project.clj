(defproject g2 "0.1.0-SNAPSHOT"

  :description "G2"
  :url "https://github.com/zeuswpi/g2"

  :dependencies [[metosin/reitit "0.5.10"]
                 [mysql/mysql-connector-java "8.0.22"]
                 [buddy "2.0.0"]
                 [cheshire "5.10.0"]
                 [clojure.java-time "0.3.2"]
                 [com.cognitect/transit-clj "1.0.324"]
                 [conman "0.9.0"]
                 [cprop "0.1.17"]
                 [funcool/struct "1.4.0"]
                 [luminus-immutant "0.2.5"]
                 [luminus-migrations "0.7.0"]
                 [luminus-transit "0.1.2"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.8.3"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.1.0"]
                 [com.google.protobuf/protobuf-java "3.13.0"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-defaults "0.3.2"]
                 [ring-cors/ring-cors "0.1.13"]
                 [selmer "1.12.31"]
                 [clj-http "3.10.3"]
                 [clj-http-fake "1.0.3"]
                 [camel-snake-kebab "0.4.2"]                ; automatic conversion between different casing of words
                 [com.clojure-goes-fast/clj-async-profiler "0.4.1"]
                 ]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot g2.core

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-immutant "2.1.0"]
            [lein-auto "0.1.2"]
            [lein-cloverage "1.1.1"]]

  :figwheel
  {:http-server-root "public"
   :server-logfile   "log/figwheel-logfile.log"
   :nrepl-port       7002
   :css-dirs         ["resources/public/css"]}

  :profiles
  {:uberjar       {:omit-source    true

                   :aot            :all
                   :uberjar-name   "g2.jar"
                   :source-paths   ["env/prod/clj"]
                   :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev   {:jvm-opts       ["-Dconf=dev-config.edn"
                                    "-Xverify:none"
                                    "-Djdk.attach.allowAttachSelf" ; for the async profiler
                                    "-XX:+UnlockDiagnosticVMOptions"
                                    "-XX:+DebugNonSafepoints"
                                    ]
                   :dependencies   [[binaryage/devtools "1.0.2"]
                                    [cider/piggieback "0.5.1"]
                                    [doo "0.1.11"]
                                    [expound "0.8.6"]
                                    [figwheel-sidecar "0.5.21-SNAPSHOT"]
                                    [pjstadig/humane-test-output "0.10.0"]
                                    [prone "2020-01-17"]
                                    [ring/ring-devel "1.8.2"]
                                    [ring/ring-mock "0.4.0"]]
                   :plugins        [[com.jakemccrary/lein-test-refresh "0.23.0"]
                                    [lein-doo "0.1.11"]
                                    [lein-figwheel "0.5.18"]]
                   :doo            {:build "test"}
                   :source-paths   ["env/dev/clj"]
                   :resource-paths ["env/dev/resources"]
                   :repl-options   {:init-ns user}
                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]}
   :project/test  {:jvm-opts       ["-Dconf=test-config.edn" "-Xverify:none"]
                   :resource-paths ["env/test/resources"]
                   :cljsbuild
                   {:builds
                    {:test
                     {:source-paths ["src/cljc"]
                      :compiler
                      {:output-to     "target/test.js"
                       :main          "g2.doo-runner"
                       :optimizations :whitespace
                       :pretty-print  true}}}}}
   :profiles/dev  {}
   :profiles/test {}}
  :repl-options {;; If nREPL takes too long to load it may timeout,
                 ;; increase this to wait longer before timing out.
                 ;; Default to 30000 (30 seconds)
                 :timeout 120000})


