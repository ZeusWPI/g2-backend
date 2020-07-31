(ns g2.services.issues-service
  (:require [g2.db.core :refer [*db*] :as db]
            [g2.services.generic-service :as generic-service]
            [g2.utils.debugging :refer [log-thread]]
            [clojure.tools.logging :as log]))

(defn get-project-issues-count [project_id]
  (-> (db/get-project-indirect-issues-count {:project_id project_id})
      log-thread
      :count))

(defn get-project-issues [project_id]
  (let [issues (db/get-project-indirect-issues {:project_id project_id})]
    (map (fn [issue]
           (log/debug (format "Issue<%s>" (str issue)))
           (-> issue
               (assoc :author {})
               (assoc :repository (generic-service/get-entity (:repo_id issue) "repos"))
               (assoc :labels [])
               (assoc :tags [])
               (dissoc :repo_id))) issues)))
