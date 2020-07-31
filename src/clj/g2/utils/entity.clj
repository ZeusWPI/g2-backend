(ns g2.utils.entity
  (:require
    [g2.db.core :refer [*db*] :as db]
    [clojure.tools.logging :as log]
    [clojure.set :as set]))

(defn get-tags [entity-type]
  (->> (db/get-tags {:table entity-type})
       (map #(set/rename-keys % {:tag_id :id}))))

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

(defn pull []
  "pull request")

#_(defn pull []
    (gen-entity "Pull" db/get-pull db/get-pulls))
