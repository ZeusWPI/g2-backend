(ns g2.git.github
  (:require [g2.config :refer [env]]
            [g2.db.core :as db]
            [ring.util.http-response :as response]
            [clj-http.client :as http]
            [slingshot.slingshot :refer [throw+ try+]]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [clojure.set :as set]))

(def base-url "https://api.github.com")

(defn fetch-convert-gh-repos []
  (let [access_token
        (db/get-repo-provider {:name "github"})
        response_body
        (:body (http/get (str base-url "/orgs" "/" (env :github-organization) "/repos?per_page=100") {:as :json}))
        converted_body (map #(-> %1
                                 (select-keys [:id :name :description :html_url])) response_body)]))

(defn sync-repositories
  "This will fetch all repositories of the organization and cache basic information in the db"
  ([]
   (sync-repositories (db/get-repo-provider {:name "github"})))
  ([access_token]
   (log/info "Syncing github repositories...")
   (try+
    (let [url (str base-url "/orgs/" (env :github-organization) "/repos?per_page=100")
          _ (log/debug "Sending request to" url)
          response-body (:body (http/get url {:as :json}))
          filtered_body (map #(select-keys %1 [:id :name :description :html_url]) response-body)
          converted_body (map #(set/rename-keys %1 {:html_url :url
                                                    :id :git_id}) filtered_body)
          remote_repo_map (reduce (fn [acc repo] (assoc acc (:git_id repo) repo)) {} converted_body)
          remote_ids (set (map :git_id converted_body))
                                        ; Local repos, id's and map
          local-repos (db/get-repos) ;TODO filter on github
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
          (db/create-repo! remote_repo)))
                                        ;Update local repositories with remote data
      (log/debug (format "Updating %d of %d repos" (count update_ids) (count common_ids)))
      (doseq [git_id update_ids]
        (let [remote_repo (get remote_repo_map git_id)]
          (db/update-repo! remote_repo)))
                                        ; TODO Handle repos that are removed on the remote
                                        ;(doseq [id remove_ids]
                                        ;  )
      (response/ok))
    (catch [:status 403] {:keys [request-time headers body]}
      (log/warn "403" request-time headers)
      (response/service-unavailable body))
    (catch [:status 404] {:keys [request-time headers body]}
      (log/warn "NOT Found 404" request-time headers body)
      (response/internal-server-error body))
    (catch Object _
      (log/error (:throwable &throw-context) "unexpected error")
      (throw+)))))

(defn create-repo-hooks [repo-id])

; admin:org -> All actions against organization webhooks require the authenticated user to be an admin of the organization being managed.

