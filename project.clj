(defproject g2 "0.1.0-SNAPSHOT"

  :description "G2"
  :url "https://github.com/zeuswpi/g2"

  :dependencies [[buddy "2.0.0"]
                 [cheshire "5.8.1"]
                 [clojure.java-time "0.3.2"]
                 [com.cognitect/transit-clj "0.8.313"]
                 [compojure "1.6.1"]
                 [conman "0.8.3"]
                 [cprop "0.1.13"]
                 [funcool/struct "1.3.0"]
                 [luminus-immutant "0.2.5"]
                 [luminus-migrations "0.6.4"]
                 [luminus-transit "0.1.1"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "1.0.7"]
                 [metosin/muuntaja "0.6.3"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"]
                 [org.clojure/tools.cli "0.4.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [mysql/mysql-connector-java "8.0.12"]
                 [com.google.protobuf/protobuf-java "3.6.1"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.6"]
                 [clj-http "3.9.1"]
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

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-immutant "2.1.0"]
            [lein-auto "0.1.2"]
            [lein-cloverage "1.1.1"]]

  :figwheel
  {:http-server-root "public"
   :server-logfile   "log/figwheel-logfile.log"
   :nrepl-port       7002
   :css-dirs         ["resources/public/css"]
   :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  :profiles
  {:uberjar       {:omit-source    true

                   :aot            :all
                   :uberjar-name   "g2.jar"
                   :source-paths   ["env/prod/clj"]
                   :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev   {:jvm-opts       ["-Dconf=dev-config.edn" "-Xverify:none"]
                   :dependencies   [[binaryage/devtools "0.9.10"]
                                    [cider/piggieback "0.4.0"]
                                    [doo "0.1.11"]
                                    [expound "0.7.2"]
                                    [figwheel-sidecar "0.5.18"]
                                    [pjstadig/humane-test-output "0.9.0"]
                                    [prone "1.6.1"]
                                    [ring/ring-devel "1.7.1"]
                                    [ring/ring-mock "0.3.2"]]
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
                     {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
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


