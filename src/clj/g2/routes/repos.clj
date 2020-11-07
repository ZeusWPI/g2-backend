(ns g2.routes.repos
  (:require
    [g2.db.core :refer [*db*] :as db]
    [g2.git.github :as git]
    [ring.util.http-response :as response]
    [slingshot.slingshot :refer [try+]]
    [conman.core :refer [with-transaction]]
    [clojure.tools.logging :as log]
    [g2.services.generic-service :as generic-service]))

(defn convert-db-to-api-object
  [db-repo]
  (-> db-repo
      (dissoc :default-tags)))

(defn repo-get
  "Fetch 1 repo"
  [repo_id]
  (let [repo (db/get-repo {:repo_id repo_id})]
    (if-not (nil? repo)
      (response/ok repo)
      (response/not-found {:msg "Repository not found"}))))

(defn repos-get [request]
  (->> (db/get-repos)
       (map (fn [repo]
              (-> repo
                  (assoc :image (format "https://zeus.gent/assets/images/Logos_%s.svg" (:name repo)))
                  (assoc :newIssueUrl "coming soon")
                  (assoc :newPullUrl "coming soon"))))
       response/ok))

(defn sync-all [_]
  (log/debug "Syncing all repositories")
  (try+
    (git/sync-repositories)
    (catch [:status 403] {:keys [body]} (do (log/error "Failed to sync repos.")
                                            (log/error "Code: 403. This can be due to ratelimiting." body)))
    (catch [:status 404] {:keys [body]} (do (log/error "Failed to sync branches."))))
  (response/no-content))

(defn route-handler-global []
  ["/repositories"
   {:swagger {:tags ["repository"]}}
   ["" {:get {:summary   "Get the list of code repositories in our backend."
              :responses {200 {}}
              :handler   repos-get}}]
   ["/sync" {:swagger {:tags ["sync"]}
             :post    {:summary   "Synchronise the data from all repositories with our database."
                       :responses {200 {:description "TODO"}
                                   403 {:description "TODO"}
                                   404 {:description "TODO"}}
                       :handler   sync-all}}]])

(defn route-handler-per-project []
  ["/repositories"
   {:swagger {:tags ["repository"]}}
   ["" {:get {:summary    "Get the repositories of a project"
              :responses  {200 {}
                           404 {:description "The project with the specified id does not exist."}}
              :parameters {:path {:id int?}}
              :handler    #(response/ok (generic-service/get-project-entities (get-in % [:path-params :id]) "repos"))}}]])
