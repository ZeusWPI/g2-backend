(ns g2.services.branches-service
  (:require [g2.db.core :refer [*db*] :as db]
            [g2.services.generic-service :as generic-service]
            [clojure.tools.logging :as log]
            ))


(defn parse-branch [branch]
  (-> branch
      (assoc :url "")                                       ;; TODO
      (assoc :repository (generic-service/get-entity (:repo_id branch) "repos"))
      (assoc :tags [])
      (assoc :featured false)
      (dissoc :repo_id)))

(defn get-project-branches [project_id]
  (let [branches (db/get-project-indirect-branches {:project_id project_id})]
    (map (fn [branch]
           (log/debug (format "Branch<%s>" (str branch)))
           (parse-branch branch)) branches)))
