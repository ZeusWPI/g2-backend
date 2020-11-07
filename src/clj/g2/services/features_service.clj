(ns g2.services.features-service
  (:require [g2.services.author-service :as author-service]
            [g2.db.core :refer [*db*] :as db]))


(defn get-project-features [project-id]
  (let [issues-features (db/get-project-features-of-type {:table "issues"
                                                          :project_id project-id})]
    (map (fn [issue] {:id     0
                      :author (author-service/dummy-author)
                      :type   "issue"
                      :data   {:issue issue}})
         issues-features)))
