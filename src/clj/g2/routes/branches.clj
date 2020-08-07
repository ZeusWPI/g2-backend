(ns g2.routes.branches
  (:require
    [g2.db.core :refer [*db*] :as db]
    [g2.git.github :refer [sync-branches]]
    [slingshot.slingshot :refer [try+]]
    [clojure.tools.logging :as log]
    [ring.util.http-response :as response]
    [g2.utils.projects :as p-utils]
    [g2.routes.tags :as tags]
    [g2.utils.entity :as entity]))

(defn get-project-branches [project_id]
  (do
    (log/debug "Get branches project" project_id)
    (p-utils/is-project
      project_id
      (response/ok (db/get-project-branches {:project_id project_id})))))


(defn sync-all [_req]
  (do
    (log/info "Syncing all branches")
    (flush)
    (let [repos (db/get-tags {:table "repos"})]
      (doseq [repo repos]
        (try+
          (sync-branches repo)
          (catch [:status 403] {:keys [body]} (do (log/error "Failed to sync branches for repo_id" (:repo_id repo))
                                                  (log/error "Code: 403. This can be due to ratelimiting." body)))
          (catch [:status 404] {:keys [body]} (do (log/error "Failed to sync branches for repo_id" (:repo_id repo) "." body)))
          (catch [:status 405] {:keys [body]} (do (log/error "Failed to sync branches for repo_id" (:repo_id repo) "." body)))
          (catch [] {} (do (log/error "Unknown error")))))
      (response/no-content))))

; Not yet needed so commented
#_(defn get-by-id [issue_id]
    (log/info "issue id: " issue_id)
    (let [issue (db/get-issue {:issue_id issue_id})]
      (log/info "issue: " issue)
      (if issue
        (response/ok issue)
        (response/not-found))))

(defn route-handler-global []
  ["/branches"
   {:swagger {:tags ["branches"]}}
   (tags/tags-route-handler (entity/branch) [])
   ["/sync"
    {:swagger {:tags ["sync"]}
     :post    {:summary   "Force synchronize the branches with our git backends. Use with limits"
               :responses {204 {:description "Sync successful"}
                           403 {:description "TODO"}
                           404 {:description "TODO"}}
               :handler   sync-all}}]]
  )

(defn route-handler-per-project []
  ["/branches"
   {:swagger {:tags ["branches"]}}
   ["" {:get {:summary    "Get the branches of a project"
              :responses  {200 {}
                           404 {:description "The project with the specified id does not exist."}}
              :parameters {:path {:id int?}}
              :handler    #(get-project-branches (get-in % [:path-params :id]))}}]])
