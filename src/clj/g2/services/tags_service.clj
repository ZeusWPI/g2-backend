(ns g2.services.tags-service
  (:require
    [g2.db.core :refer [*db*] :as db]
    [clojure.tools.logging :as log]
    [clojure.set :as set]))


(defn assert-id-of-entity
  "Used to throw 404's if an entity doesn't exist"
  [entity-id entity f]
  (log/debug (format "Checking validity of entity of type '%s' with id '%s'" entity entity-id))
  (let [db-object (db/get-tag {:table entity :tag_id entity-id})]
    (if (nil? db-object)
      (do
        (log/debug "Entity not found")
        (throw
          (ex-info "not-found"
                   {:causes #{"Entity not found in the database."}})))
      (do
        (log/debug (format "Entity valid: %s" db-object))
        (-> db-object
            (set/rename-keys {:tag_id :id})
            f)))))

(defn get-tags-linked-with-tag
  [tag-id entity-parent entity-child]
  (log/debug (format "Fetching %s for %s<id: %s>" entity-child entity-parent tag-id))
  (->>
    (db/get-tags-linked-with-tag {:table entity-child :tag_id tag-id})
    ((fn [obj] (log/debug obj) obj))
    (map #(dissoc % :parent_id :child_id))
    (map #(set/rename-keys % {:tag_id :id}))))


(defn get-tags-count-linked-with-tag
  [tag-id entity-parent entity-child]
  (log/debug (format "Fetching %s for %s<id: %s>" entity-child entity-parent tag-id))
  (->>
    (db/get-tags-count-linked-with-tag {:table entity-child :tag_id tag-id})
    ((fn [obj] (log/debug obj) obj))))

(defn assert-get-tags-linked-with-tag
  "Extracts the parent id from the request, returns the linked tags of type 'entity-child'"
  [entity-id entity-parent entity-child]
  (assert-id-of-entity entity-id entity-parent
                       (fn [{tag-id :id}]
                         (get-tags-linked-with-tag tag-id entity-parent entity-child))))

(defn assert-get-tags-count-linked-with-tag
  "Extracts the parent id from the request, returns the linked tags of type 'entity-child'"
  [parent-id parent-type child-type]
  (assert-id-of-entity parent-id parent-type
                       (fn [{tag-id :id}]
                         (get-tags-count-linked-with-tag tag-id parent-type child-type))))
