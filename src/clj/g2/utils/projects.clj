(ns g2.utils.projects
  (:require
    [g2.db.core :refer [*db*] :as db]
    [clojure.tools.logging :as log]
    [ring.util.http-response :as response]))

(defn is-project [project_id success]
  (let [project (db/get-project project_id)]
    (if (nil? project)
      (response/not-found)
      (success))))

(defn get-all-for-project [project_id [^String what] query-fun]
  (do
    (log/debug "Get " what " project" project_id)
    (is-project
      project_id
      (response/not-found)
      (response/ok (query-fun {:project_id project_id})))))