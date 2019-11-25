(ns user
  (:require [g2.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [g2.figwheel :refer [start-fw stop-fw cljs]]
            [g2.core :refer [start-app]]
            [g2.db.core]
            [conman.core :as conman]
            [luminus-migrations.core :as migrations]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start
  "Starts application.
  You'll usually want to run this on startup."
  []
  (println "Starting the server, please wait, this can take some time...")
  (mount/start-without #'g2.core/repl-server))

(defn stop
  "Stops application."
  []
  (mount/stop-except #'g2.core/repl-server))

(defn restart
  "Restarts application."
  []
  (stop)
  (start))

(defn restart-db []
  (mount/stop #'g2.db.core/*db*)
  (mount/start #'g2.db.core/*db*)
  (binding [*ns* 'g2.db.core]
    (conman/bind-connection g2.db.core/*db* "sql/queries.sql")))

(defn reset-db []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate []
  "apply pending migrations"
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  "rollback the last migration applied"
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration [name]
  "Rollback all migrations that have been run, and then apply all migrations."
  (migrations/create name (select-keys env [:database-url])))


