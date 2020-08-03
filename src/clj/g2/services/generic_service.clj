(ns g2.services.generic-service
  (:require [g2.services.tags-service :as tags-service]
            [g2.db.core :refer [*db*] :as db]
            [g2.utils.debugging :as debugging]))

(defn get-entity [entity-id entity-type]
  (tags-service/assert-id-of-entity entity-id entity-type identity))

(defn delete-entity [entity-id entity-type]
  (db/delete-entity! {:table  entity-type
                      :tag_id entity-id})
  (db/delete-tag! {:id entity-id}))

(defn feature-entity [entity-id]
  (db/set-feature-tag! {:tag_id entity-id
                       :featured true}))

(defn unfeature-entity [entity-id]
  (db/set-feature-tag! {:tag_id entity-id
                       :featured false}))

(defn get-project-entities [project-id entity-type]
  "Get the entities of a type of a project"
  (tags-service/assert-get-tags-linked-with-tag project-id "projects" entity-type))

(defn get-project-entities-count [project-id entity-type]
  (-> (tags-service/assert-get-tags-count-linked-with-tag project-id "projects" entity-type)
      debugging/log-thread
      :count))
