(ns g2.services.projects-service
  (:require [g2.utils.entity :as entity]
            [g2.services.tags-service :as tags-service]
            [g2.services.generic-service :as generic-service]
            [g2.utils.debugging :refer [log-thread]]
            [g2.routes.tags :as tags]
            [g2.services.repos-service :as repos-service]))


(defn construct-project-from-base [project]
  (-> project
      (assoc :statistics {:issuesCount       0
                          :repositoriesCount (generic-service/get-project-entities-count (:id project) "repos")
                          :pullsCount        0})
      (assoc :tags (tags-service/get-tags-linked-with-tag (:id project) "projects" "named_tags"))))

(defn project-get [project-id]
  "Check that the project exists.
  Convert the retrieved object to our needed model."
  (tags-service/assert-id-of-entity project-id "projects" construct-project-from-base))

(defn projects-get []
  (->>
    (entity/get-tags "projects")
    log-thread
    (map construct-project-from-base)))
