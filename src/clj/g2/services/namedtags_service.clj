(ns g2.services.namedtags-service
  (:require
    [g2.db.core :refer [*db*] :as db]))


(defn namedtags-get []
  (db/get-tags {:table "named_tags"}))

(defn namedtag-get [namedtag_id]
  (db/get-tag {:table "named_tags" :tag_id namedtag_id}))

(defn namedtag-create [data]
  (let [{tag_id :generated_key} (db/create-tag!)]
    (db/create-generic! {:table "named_tags"
                         :data  (assoc data :tag_id tag_id)})
    tag_id))
