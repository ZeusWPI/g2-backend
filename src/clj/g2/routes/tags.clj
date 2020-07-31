(ns g2.routes.tags
  (:require [ring.util.http-response :as response]
            [g2.db.core :refer [*db*] :as db]
            [g2.utils.entity :as entity]
            [clojure.tools.logging :as log]
            [clojure.set :as set]
            [g2.services.tags-service :as tags-service])
  (:use [slingshot.slingshot :only [throw+ try+]])
  (:import (java.util List)))

(defn get-entity [db-object]
  (response/ok db-object))

; TODO implement this using the allowed-links list, these are tho only entities that are allowed to link with this entity
(defn tag-entity-with [{{tag-id :tag entity-id :id} :path-params :as req} entity]
  (tags-service/assert-id-of-entity
    entity-id entity
    (fn [x]
      (let [db-object-id (get x :id)]
        (log/debug (format "Linking '%s' with '%s'" db-object-id tag-id))
        (db/link-tag! {:parent_id db-object-id :child_id tag-id})
        (response/ok)))))

(defn untag-entity-with [{{tag-id :tag entity-id :id} :path-params :as req} entity]
  (tags-service/assert-id-of-entity
    entity-id entity
    (fn [x]
      (let [db-object-id (get x :id)]
        (log/debug (format "Linking '%s' with '%s'" db-object-id tag-id))
        (db/unlink-tag! {:parent_id db-object-id :child_id tag-id})
        (response/ok)))))

(defn tags-operations-route-handler
  ([entity allowed-links]
   (tags-operations-route-handler entity allowed-links "link"))
  ([^String entity ^List allowed-links link-path-name]      ; TODO implement allowed-links
   (let [what entity]
     [(str "/" link-path-name)
      ["/:tag" {:post   {:summary    (str "Tag an " what " with a specific tag.")
                         :responses  {200 {}
                                      404 {:description (str "The " what " or Tag with the specified id does not exist.")}}
                         :parameters {:path {:id  int?
                                             :tag int?}}
                         :handler    #(tag-entity-with % entity)}
                :delete {:summary    (str "Remove specific tag from " what ".")
                         :responses  {200 {}
                                      404 {:description (str "The " what " or Tag with the specified id does not exist.")}}
                         :parameters {:path {:id  int?
                                             :tag int?}}
                         :handler    #(untag-entity-with % entity)}}]])))

(defn tags-route-handler
  "A generic handler that can possible be used by an entity type that does no further conversions or joins."
  ([entity allowed-links]
   (tags-route-handler entity allowed-links "link"))
  ([entity allowed-links link-path-name]
   ["/:id"
    ["" {:get {:summary    (str "Get a " entity " by id")
               :responses  {200 {}
                            404 {:description (str "The " entity " with the specified id does not exist.")}}
               :parameters {:path {:id int?}}
               :handler    #(tags-service/assert-id-of-entity (get-in % [:path-params :id]) entity get-entity)}}]
    (tags-operations-route-handler entity allowed-links link-path-name)]))
