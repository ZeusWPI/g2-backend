(ns g2.services.validator-service
  (:require
    [g2.db.core :refer [*db*] :as db]))

(defn validate-is-project [project-id?]
  (> (:count (db/count-entity {:table  "projects"
                               :tag_id project-id?}))
     0))
