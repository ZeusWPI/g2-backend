(ns g2.services.projects-service
  (:require [g2.utils.entity :as entity]
            [g2.db.core :refer [*db*] :as db]
            [g2.routes.tags :as tags]
            [g2.services.generic-service :as generic-service]
            [g2.services.tags-service :as tags-service]
            [g2.services.repos-service :as repos-service]
            [g2.services.issues-service :as issues-service]
            [g2.utils.debugging :refer [log-thread]]
            [conman.core :refer [with-transaction]]
            [clojure.tools.logging :as log]
            [clojure.set :as set]))


(defn parse-project [project]
  (-> project
      (assoc :statistics {:issuesCount 0 :repositoriesCount 0 :pullsCount 0})
      (assoc :tags [])
      (assoc :featured false)))

(defn flip [f] (fn [x y & args] (apply f y x args)))

(defn- construct-project-from-base-bare [{project_id :id :as project}]
  (-> project
      (set/rename-keys {:image_url :image})))

(defn construct-project-from-base [{project_id :id :as project}]
  (-> project
      construct-project-from-base-bare
      (assoc :statistics {:issuesCount       (issues-service/get-project-issues-count project_id)
                          :repositoriesCount (generic-service/get-project-entities-count project_id "repos")
                          :pullsCount        0})
      (assoc :tags (tags-service/get-tags-linked-with-tag project_id "projects" "named_tags"))))



(defn project-get [project_id]
  "Check that the project exists.
  Convert the retrieved object to our needed model."
  (tags-service/assert-id-of-entity project_id "projects" construct-project-from-base))

(defn projects-get []
  (->>
    (entity/get-tags "projects")
    log-thread
    (map construct-project-from-base)))

(defn project-edit [project_id new_values]
  (do
    (log/debug "Update project" project_id " new values" new_values)
    (let [project (tags-service/assert-id-of-entity project_id "projects" construct-project-from-base-bare)]
      (log/debug (format "Got obj: %s" (str project)))
      (-> project
          (merge new_values)
          log-thread
          (db/update-project!)))))

(defn get-projects-with [q]
  (map parse-project (db/get-projects-by-name-like {:q (format "%%%s%%" q)})))
