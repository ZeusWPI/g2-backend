(ns g2.services.generic-service
  (:require [g2.services.tags-service :as tags-service]))

(defn get-project-entities [project-id entity-type]
  "Get the entities of a type of a project"
  (tags-service/assert-get-tags-linked-with-tag project-id "projects" entity-type))

(defn get-project-entities-count [project-id entity-type]
  (-> (tags-service/assert-get-tags-count-linked-with-tag project-id "projects" entity-type)
      g2.utils.debugging/log-thread
      :count))
