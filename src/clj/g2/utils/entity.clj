(ns g2.utils.entity
  (:require
    [g2.db.core :refer [*db*] :as db]
    [clojure.tools.logging :as log]))

(defn get-tag [type tag-id]
  (db/get-tag {:table type :tag_id tag-id}))

(defn get-tags [type]
  (db/get-tags {:table type}))

(defn project []
  "projects")

(defn repository []
  "repos")
;
(defn issue []
  "issues")
;
(defn branch []
  "branches")

#_(defn pull []
    (gen-entity "Pull" db/get-pull db/get-pulls))
