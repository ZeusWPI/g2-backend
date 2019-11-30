(ns g2.git.github
  (:require [g2.config :refer [env]]
            [g2.db.core :as db]
            [clj-http.client :as http]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [clojure.set :as set]))

(def base-url "https://api.github.com")

(defn sync-repositories
  "Fetch all repositories of the organization.
  Merge existing information with new updates.
  Add new information
  "
  ([]
   (sync-repositories (db/get-repo-provider {:name "github"})))
  ([access_token]
   (fetch-and-sync-with-local (str "/orgs" "/" (env :github-organization) "/repos?per_page=100")
                              {:id :git_id
                               :name :name
                               :description :description
                               :html_url :url}
                              :git_id ; local and remote shared unique identifier
                              db/get-repos ;; TODO filter to only fetch github repos
                              db/create-repo!
                              db/update-repo!)))

(defn sync-labels
  [repo-id]
  (let [name (:name (db/get-repo {:repo_id repo-id}))]
    (fetch-and-sync-with-local (str "/repos/" (env :github-organization) "/" name "/labels")
                               {:id :git_id
                                :url :url
                                :name :name
                                :color :color
                                :description :description
                                }
                               :git_id
                               db/get-labels
                               #(db/create-label! (assoc % :repo_id repo-id))
                               db/update-label!
                               )
    ))

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
  [url-path property-mapping shared-identifier local-query-get local-query-create local-query-update]
  (log/debug "==============")
  (log/info "Syncing with endpoint" url-path)
  (let [remote-data (->> (http/get (str base-url url-path) {:as :json})
                         (:body)
                         (map #(-> %1
                                   (select-keys (keys property-mapping))
                                   (set/rename-keys property-mapping)
                                   (update shared-identifier str))))
        remote-entity-map (reduce (fn [acc entity] (assoc acc (shared-identifier entity) entity)) {} remote-data)
        remote-ids (set (map shared-identifier remote-data))

        local-data (map #(select-keys % (vals property-mapping)) (local-query-get))
        local-entity-map (reduce (fn [acc entity] (assoc acc (shared-identifier entity) entity)) {} local-data)
        local-ids (set (map shared-identifier local-data))

        new-ids (clojure.set/difference remote-ids local-ids)
        common-ids (clojure.set/intersection remote-ids local-ids)
        update-ids (filter (fn [id] (not= (remote-entity-map id) (local-entity-map id))) common-ids)
        remove-ids (clojure.set/difference local-ids remote-ids) ; TODO handle entities that are removed on github
        ]
                                        ; Create new entities that are not in our db
    (log/debug (format "Creating %d new objects" (count new-ids)))
    (doseq [id new-ids]
      (let [remote-entity (get remote-entity-map id)]
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

(defn create-repo-hooks [repo-id])

; admin:org -> All actions against organization webhooks require the authenticated user to be an admin of the organization being managed.

