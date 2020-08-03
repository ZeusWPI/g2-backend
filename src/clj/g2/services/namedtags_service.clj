(ns g2.services.namedtags-service
  (:require
    [g2.db.core :refer [*db*] :as db]
    [g2.services.tags-service :as tags-service]
    [clojure.tools.logging :as log]))


(defn namedtags-get []
  (db/get-tags {:table "named_tags"}))

(defn namedtag-get [namedtag_id]
  (db/get-tag {:table "named_tags" :tag_id namedtag_id}))

(defn namedtag-create [data]
  (let [{tag_id :generated_key} (db/create-tag!)]
    (db/create-generic! {:table "named_tags"
                         :data  (assoc data :tag_id tag_id)})
    tag_id))

(defn namedtag-edit [id new_values]
  (log/debug (format "Update named tag<id: %s> with new values %s" id (str new_values)))
  (let [namedtag (-> (tags-service/assert-id-of-entity id "named_tags" identity)
                     (dissoc :id))]
    (log/debug (format "Original object: %s" (str namedtag)))
    (log/debug (format "With new vals  : %s" (str (merge namedtag new_values))))
    (db/update-generic! {:table   "named_tags"
                         :updates (merge namedtag new_values)
                         :id      id})))
