(ns g2.github
  (:require [g2.config :refer [env]]
            [g2.db.core :as db]
            [clj-http.client :as http]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]))

(defn oauth2-params []
  {:client-id        (env :github-oauth-consumer-key)
   :client-secret    (env :github-oauth-consumer-secret)
   :authorize-uri    (env :github-authorize-uri)
   :redirect-uri     (str (env :app-host) "/oauth/github-callback")
   :access-token-uri (env :github-access-token-uri)
   :scope "admin:org admin:org_hook"})

(def base-url "https://api.github.com")

(defn sync-repositories
  "This will fetch all repositories of the organization and cache basic information in the db"
  ([]
   (sync-repositories (db/get-repo-provider {:name "github"})))
  ([access_token]
   (log/info "Syncing github repositories...")
   (let [response-body (:body (http/get (str base-url "/orgs" "/" (env :github-organization) "/repos?per_page=100") {:as :json}))
         remote_repo_map (reduce (fn [acc repo] (assoc acc (:id repo) (select-keys repo [:id :name :description]))) {} response-body)
         remote_ids (set (map :id response-body))
         ; Local repos, id's and map
         local-repos (db/get-github-repos)
         local_repo_map (reduce (fn [acc repo] (assoc acc (:id repo) repo)) {} local-repos)
         local_ids (set (map :id local-repos))
         ; Calculated id sets
         new_ids (clojure.set/difference remote_ids local_ids)
         common_ids (clojure.set/intersection remote_ids local_ids)
         update_ids (filter (fn [id] (not= (remote_repo_map id) (local_repo_map id))) common_ids)
         remove_ids (clojure.set/difference local_ids remote_ids) ; TODO handle repos that are removed on github
         ]
     ; Create new repositories that are not in our db
     (log/debug "==============")
     (log/debug (format "Creating %d new repos" (count new_ids)))
     (doseq [id new_ids]
       (let [remote_repo (get remote_repo_map id)]
         (db/create-github-repo! remote_repo)))
     ;Update local repositories with remote data
     (log/debug (format "Updating %d of %d repos" (count update_ids) (count common_ids)))
     (doseq [id update_ids]
       (let [remote_repo (get remote_repo_map id)]
         (db/update-github-repo! remote_repo)))
     ; TODO Handle repos that are removed on the remote
     ;(doseq [id remove_ids]
     ;  )
     )))

(defn create-repo-hooks [repo-id])

; admin:org -> All actions against organization webhooks require the authenticated user to be an admin of the organization being managed.

