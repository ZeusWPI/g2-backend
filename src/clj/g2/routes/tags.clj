(ns g2.routes.tags
  (:require [ring.util.http-response :as response]
            [g2.db.core :refer [*db*] :as db]
            [g2.utils.entity :as entity]
            [clojure.tools.logging :as log]
            [clojure.set :as set])
  (:use [slingshot.slingshot :only [throw+ try+]])
  (:import (java.util List)))

(defn assert-id-of-entity
  "Used to throw 404's if an entity doesn't exist"
  [req entity f]
  (let [entity-id (get-in req [:path-params :id])]
    (log/debug (format "Checking validity of entity of type '%s' with id '%s'" entity entity-id))
    (let [db-object (entity/get-tag entity entity-id)]
      (if (nil? db-object)
        (do
          (log/debug "Entity not found")
          (throw
            (ex-info "not-found"
                     {:causes #{"Entity not found in the database."}})))
        (do
          (log/debug (format "Entity valid: %s" db-object))
          (f db-object))))))

(defn get-tags-linked-with-tag
  "Extracts the parent id from the request, returns the linked tags of type 'entity-child'"
  [req entity-parent entity-child]
  (assert-id-of-entity req entity-parent
                       (fn [{tag-id :tag_id}]
                         (log/debug (format "Fetching %s for %s<id: %s>" entity-child entity-parent tag-id))
                         (->>
                           (db/get-tags-linked-with-tag {:table entity-child :tag_id tag-id})
                           ((fn [obj] (log/debug obj) obj))
                           (map #(dissoc % :parent_id :child_id))
                           (map #(set/rename-keys % {:tag_id :id}))))))

(defn get-entity [db-object]
  (response/ok db-object))

; TODO implement this using the allowed-links list, these are tho only entities that are allowed to link with this entity
(defn tag-entity-with [db-object-id tag-id]
  (log/debug (format "Linking '%s' with '%s'" db-object-id tag-id))
  (db/link-tag! {:parent_id db-object-id :child_id tag-id})
  (response/ok))

(defn untag-entity-with [db-object-id tag-id]
  (db/unlink-tag! {:parent_id db-object-id :child_id tag-id})
  (response/ok))

(defn tags-operations-route-handler [^String entity ^List allowed-links] ; TODO implement allowed-links
  (let [what entity]
    ["/tags"
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
  "A generic handler that can possible be used by an entity type that does no further conversions or joins."
  ["/:id"
   ["" {:get {:summary    (str "Get a " entity " by id")
              :responses  {200 {}
                           404 {:description (str "The " entity " with the specified id does not exist.")}}
              :parameters {:path {:id int?}}
              :handler    #(assert-id-of-entity % entity get-entity)}}]
   (tags-operations-route-handler entity allowed-links)])
