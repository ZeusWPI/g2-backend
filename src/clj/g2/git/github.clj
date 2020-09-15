(ns g2.git.github
  (:require [g2.config :refer [env]]
            [g2.db.core :as db]
            [clj-http.client :as http]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [java-time :refer [local-date-time zoned-date-time]]
            [g2.utils.entity :as entity]))

(s/def ::seq-of-keywords (s/* keyword?))

;; Some utils
(defn compress-keyword-vec
  [keywords]
  {:pre [s/valid? ::seq-of-keywords keywords]}
  (apply keyword (map name keywords)))

(defn select-keys*
  "select keys recursively in a nested map"
  [m paths]
  (into {} (map (fn [p]
                  (if (vector? p)
                    [(compress-keyword-vec p) (get-in m p)]
                    [p (get m p)])))
        paths))

(def date-properties
  "All properties that are in this set will be automatically converted to a zoned-date-time"
  [:created_at])

(defn apply-date-conversion [m]
  (last (for [prop date-properties]
          (if (contains? m prop)
            (update m prop #(local-date-time (zoned-date-time %)))
            m))))

(defn apply-keyword-compression
  "Applies the keyword compression utility onto every key that is a list"
  [m]
  {:pre [s/valid? map? m]}
  (reduce-kv (fn [acc k v] (assoc acc (if (vector? k) (compress-keyword-vec k) k) v)) {} m))

(def base-url "https://api.github.com")

(defn fetch-and-sync-with-local
  "This function can now be used for any synchronization
  operation with an api and our database. You only specify the endpoint, the
  needed properties and the relevant sql (queries.)

  Removal is atm not supported. This will probably be implemented with gravestones.

  ASSUMPTIONS
    * The shared primary key is a string in our database
    * The specified api endpoint return a simple list with the entities we want to synchronize

  Function params:
    * property mapping: The keys selected from original maps and how they will be renamed
    * shared-identifier: The name of the property that will be used to check which entities we already have in our database and which we don't. This is after property renaming for the remote entities.
  "
  [url property-mapping shared-identifier exclude-key local-query-get local-query-create local-query-update]
  (log/debug "==============")
  (log/info "Syncing with endpoint" url)
  (let [remote-data (->> (http/get url {:headers {"Authorization" (str "token " (env :github-personal-access-token))}
                                        :as      :json})
                         (:body)
                         (filter #(or (nil? exclude-key) (not (contains? % exclude-key))))
                         (map #(-> %1
                                   (select-keys* (keys property-mapping))
                                   (apply-date-conversion)
                                   (set/rename-keys (apply-keyword-compression property-mapping))
                                   (update shared-identifier str))))
        remote-entity-map (reduce (fn [acc entity] (assoc acc (shared-identifier entity) entity)) {} remote-data)
        remote-ids (set (map shared-identifier remote-data))

        local-data (map #(select-keys % (vals property-mapping)) (local-query-get))
        local-entity-map (reduce (fn [acc entity] (assoc acc (shared-identifier entity) entity)) {} local-data)
        local-ids (set (map shared-identifier local-data))

        new-ids (clojure.set/difference remote-ids local-ids)
        common-ids (clojure.set/intersection remote-ids local-ids)
        update-ids (filter (fn [id] #_(println (clojure.data/diff (remote-entity-map id) (local-entity-map id)) "\n") (not= (remote-entity-map id) (local-entity-map id))) common-ids)
        remove-ids (clojure.set/difference local-ids remote-ids) ; TODO handle entities that are removed on github
        ]
    ; Create new entities that are not in our db
    (log/debug (format "Creating %d new objects" (count new-ids)))
    (doseq [id new-ids]
      (let [remote-entity (get remote-entity-map id)]
        (println "Creating" remote-entity)
        (local-query-create remote-entity)))
    ;Update local entities with their remote data
    (log/debug (format "Updating %d of %d objects" (count update-ids) (count common-ids)))
    (doseq [id update-ids]
      (let [remote-entity (get remote-entity-map id)]
        (local-query-update remote-entity)))
    ; TODO Handle entities that are removed on the remote
    ;(doseq [id remove-ids]
    ;  )
    ))

(def github-endpoints {:repos    (fn [] (str base-url "/orgs/" (env :github-organization)
                                             "/repos?per_page=100"))
                       :labels   (fn [repo-name] (str base-url "/repos/" (env :github-organization) "/"
                                                      repo-name "/labels"))
                       :issues   (fn [repo-name] (str base-url "/repos/" (env :github-organization) "/"
                                                      repo-name "/issues"))
                       :branches (fn [repo-name] (str base-url "/repos/" (env :github-organization) "/"
                                                      repo-name "/branches"))})



(defn sync-repositories
  "Fetch all repositories of the organization.
  Merge existing information with new updates.
  Add new information
  "
  ([]
   (sync-repositories (db/get-repo-provider {:name "github"})))
  ([access_token]
   (fetch-and-sync-with-local ((github-endpoints :repos))
                              {:id          :git_id
                               :name        :name
                               :description :description
                               :html_url    :url}
                              :git_id                       ; local and remote shared unique identifier
                              nil
                              (do (doall (map println (db/get-tags {:table (entity/repository)})))
                                  (fn [] (filter (fn [repo] (= (get repo :repo_type) "github")) (db/get-tags {:table (entity/repository)})))
                                  )
                              #(db/create-repo! (assoc % :tag_id (entity/generate-tag)))
                              db/update-repo!)))

(defn sync-labels
  [{name :name repo_id :tag_id :as repo}]
  (fetch-and-sync-with-local ((github-endpoints :labels) name)
                             {:id          :git_id
                              :name        :name
                              :description :description
                              :url         :url
                              :color       :color}
                             :git_id
                             nil
                             db/get-labels
                             #(db/create-label! (-> %
                                                    (assoc :tag_id (entity/generate-tag))
                                                    (assoc :repo_id repo_id)))
                             db/update-label!))

(defn sync-issues
  [{name :name repo_id :tag_id :as repo}]
  (log/debug (format "Syncing issues for '%s'" (str repo)))
  (fetch-and-sync-with-local ((github-endpoints :issues) name)
                             {:id         :git_id
                              :html_url   :url
                              :title      :title
                              :created_at :time
                              :state      :status
                              [:user :id] :author}
                             :git_id
                             :pull_request
                             db/get-issues
                             #(db/create-issue! (-> %
                                                    (assoc :tag_id (entity/generate-tag))
                                                    (assoc :repo_id repo_id)))
                             db/update-issue!))

; TODO
#_(defn sync-pullrequests
  [{name :name repo_id :tag_id :as repo}]
  (log/debug (format "Syncing issues for '%s'" (str repo)))
  (fetch-and-sync-with-local ((github-endpoints :issues) name)
                             {:id         :git_id
                              :html_url   :url
                              :title      :title
                              :created_at :time
                              :state      :status
                              [:user :id] :author}
                             :git_id
                             :pull_request
                             db/get-issues
                             #(db/create-issue! (-> %
                                                    (assoc :tag_id (entity/generate-tag))
                                                    (assoc :repo_id repo_id)))
                             db/update-issue!))

(defn sync-branches
  [{name :name repo_id :tag_id :as repo}]
  (log/debug (format "Syncing branches for '%s'" (str repo)))
  (flush)
  (fetch-and-sync-with-local ((github-endpoints :branches) name)
                             {[:commit :sha] :commit_sha
                              :name          :name}
                             :commit_sha
                             nil
                             #(db/get-tags {:tables "branches"})
                             #(db/create-branch! (-> %
                                                     (assoc :tag_id (entity/generate-tag))
                                                     (assoc :repo_id repo_id)))
                             db/update-branch!))

(defn create-repo-hooks [repo-id])

; admin:org -> All actions against organization webhooks require the authenticated user to be an admin of the organization being managed.
