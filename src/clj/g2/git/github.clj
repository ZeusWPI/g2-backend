(ns g2.git.github
  (:require [g2.config :refer [env]]
            [g2.db.core :as db]
            [clj-http.client :as http]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [clojure.set :as set]))

(def base-url "https://api.github.com")

(defn sync-repositories
  "This will fetch all repositories of the organization and cache basic information in the db"
  ([]
   (sync-repositories (db/get-repo-provider {:name "github"})))
  ([access_token]
   (log/info "Syncing github repositories...")
   (let [response-body (:body (http/get (str base-url "/orgs" "/" (env :github-organization) "/repos?per_page=100") {:as :json}))
         remote_repo_map (reduce (fn [acc repo]
                                   (assoc acc (:id repo)
                                          (set/rename-keys (select-keys repo [:id :name :description :html_url])
                                                           {:html_url :url}))) {} response-body)
         remote_ids (set (map :id response-body))
         ; Local repos, id's and map
         local-repos (db/get-repos) ;TODO flter on github
         local_repo_map (reduce (fn [acc repo] (assoc acc (:git_id repo) repo)) {} local-repos)
         local_ids (set (map :git_id local-repos))
         ; Calculated id sets
         new_ids (clojure.set/difference remote_ids local_ids)
         common_ids (clojure.set/intersection remote_ids local_ids)
         update_ids (filter (fn [id] (not= (remote_repo_map id) (local_repo_map id))) common_ids)
         remove_ids (clojure.set/difference local_ids remote_ids) ; TODO handle repos that are removed on github
         ]
     ; Create new repositories that are not in our db
     (log/debug "==============")
     (log/debug (format "Creating %d new repos" (count new_ids)))
     (doseq [git_id new_ids]
       (let [remote_repo (get remote_repo_map git_id)]
         (db/create-repo! (set/rename-keys remote_repo {:id :git_id}))))
     ;Update local repositories with remote data
     (log/debug (format "Updating %d of %d repos" (count update_ids) (count common_ids)))
     (doseq [git_id update_ids]
       (let [remote_repo (get remote_repo_map git_id)]
         (db/update-repo! (assoc remote_repo :git_id (:id remote_repo)))))
     ; TODO Handle repos that are removed on the remote
     ;(doseq [id remove_ids]
     ;  )
     )))

(defn create-repo-hooks [repo-id])

; admin:org -> All actions against organization webhooks require the authenticated user to be an admin of the organization being managed.

