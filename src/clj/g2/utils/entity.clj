(ns g2.utils.entity
  (:require
    [g2.db.core :refer [*db*] :as db]
    [clojure.tools.logging :as log]))

(defn get-entity-info [entity key]
  (do
    (log/debug entity key)
    (let [info (get-in entity [key])]
      (if (nil? info)
        (throw (Exception. (str "not a taggable: " entity)))
        info))))

(defn -name [entity]
  (get-entity-info entity :name))

(defn -get [entity]
  (get-entity-info entity :get))

(defn -get-all [entity]
  (get-entity-info entity :get-all))

(defn gen-entity [^String name get get-all]
  (hash-map :name    name
            :get     get
            :get-all get-all))

(defn project []
  (gen-entity "Project" db/get-project db/get-projects))

(defn repository []
  (gen-entity "Repository" db/get-repo db/get-repos))

(defn issue []
  (gen-entity "Issue" db/get-issue db/get-issues))

(defn branch []
  (gen-entity "Branch" db/get-branch db/get-branches))

#_(defn pull []
    (gen-entity "Pull" db/get-pull db/get-pulls))