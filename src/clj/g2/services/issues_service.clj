(ns g2.services.issues-service
  (:require [g2.db.core :refer [*db*] :as db]
            [g2.services.generic-service :as generic-service]
            [clojure.tools.logging :as log]))


(defn issues-get-per-project [project_id]
  (let [issues (db/get-indirect-issues-per-project {:project_id project_id})]
    (map (fn [issue]
           (log/debug (format "Issue<%s>" (str issue)))
           (-> issue
               (assoc :author {})
               (assoc :repository (generic-service/get-entity (:repo_id issue) "repos"))
               (assoc :labels [])
               (assoc :tags [])
               (dissoc :repo_id))) issues)))
