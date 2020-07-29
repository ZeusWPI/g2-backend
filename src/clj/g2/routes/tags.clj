(ns g2.routes.tags
  (:require [ring.util.http-response :as response]
            [g2.db.core :refer [*db*] :as db]
            [g2.utils.entity :as entity]
            [clojure.tools.logging :as log])
  (:import (java.util List)))

(defn assert-id-of-entity
  "Used to throw 404's if an entity doesn't exist"
  ([req entity f]
   (let [entity-id (get-in req [:path-params :id])]
     (log/debug (format "Checking validity of entity of type '%s' with id '%s'" entity entity-id))
     (let [db-object (entity/get-tag entity entity-id)]
       (if (nil? db-object)
         (do
           (log/debug "Entity not found")
           (response/not-found))
         (do
           (log/debug (format "Entity valid: %s" db-object))
           (f db-object))))))
  ([req entity]
   (assert-id-of-entity req entity response/ok)))

(defn get-entity [db-object]
  (response/ok db-object))

; TODO write a db-query to fetch the named-tags of an entity
(defn get-named-tags [db-object]
  (response/ok []))

; TODO implement this using the allowed-links list, these are tho only entities that are allowed to link with this entity
(defn tag-entity-with [db-object-id tag-id]
  (log/debug (format "Linking '%s' with '%s'" db-object-id tag-id))
  (db/link-tag! {:parent_id db-object-id :child_id tag-id})
  (response/ok))

(defn untag-entity-with [db-object-id tag-id]
  (db/unlink-tag! {:parent_id db-object-id :child_id tag-id})
  (response/ok))

(defn tags-operations-route-handler [^String entity ^List allowed-links]
  (let [what entity]
    ["/tags"
     ["" {:get {:summary    (str "Get named-tags associated with " what ".")
                :responses  {200 {}
                             404 {:description (str "The " what " with the specified id does not exist.")}}
                :parameters {:path {:id int?}}
                :handler    #(assert-id-of-entity % entity get-named-tags)}}]
     ["/:tag" {:post   {:summary    (str "Tag an " what " with a specific tag.")
                        :responses  {200 {}
                                     404 {:description (str "The " what " or Tag with the specified id does not exist.")}}
                        :parameters {:path {:id  int?
                                            :tag int?}}
                        :handler    #(assert-id-of-entity % entity (fn [x] (tag-entity-with (get x :tag_id) (get-in % [:path-params :tag]))))}
               :delete {:summary    (str "Remove specific tag from " what ".")
                        :responses  {200 {}
                                     404 {:description (str "The " what " or Tag with the specified id does not exist.")}}
                        :parameters {:path {:id  int?
                                            :tag int?}}
                        :handler    #(assert-id-of-entity % entity (fn [x] (untag-entity-with (get x :tag_id) (get-in % [:path-params :tag]))))}}]]))

(defn tags-route-handler [entity allowed-links]
  ["/:id"
   ["" {:get {:summary    (str "Get a " entity " by id")
              :responses  {200 {}
                           404 {:description (str "The " entity " with the specified id does not exist.")}}
              :parameters {:path {:id int?}}
              :handler    #(assert-id-of-entity % entity get-entity)}}]
   (tags-operations-route-handler entity allowed-links)])
