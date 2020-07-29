(ns g2.routes.issues
  (:require [g2.db.core :refer [*db*] :as db]
            [g2.git.github :refer [sync-issues]]
            [slingshot.slingshot :refer [try+]]
            [clojure.tools.logging :as log]
            [ring.util.http-response :as response]
            [g2.utils.projects :as p-util]
            [g2.routes.tags :as tags]
            [g2.utils.entity :as entity]))

(defn get-project-issues [project_id]
  (do
    (log/debug "Get issues project" project_id)
    (p-util/is-project
      project_id
      (response/ok (db/get-project-issues {:project_id project_id})))))

(defn sync-all [req]
  (let [repos (db/get-repos)]
    (doseq [repo repos]
      (try+
        (sync-issues (:repo_id repo))
        (catch [:status 403] {:keys [body]} (do (log/error "Failed to sync repo_id" (:repo_id repo))
                                                (log/error "Code: 403. This can be due to ratelimiting." body)))
        (catch [:status 404] {:keys [body]} (do (log/error "Failed to sync repo_id" (:repo_id repo) "." body)))))
    (response/ok)))

; Not yet needed so commented
#_(defn get-by-id [issue_id]
    (log/info "issue id: " issue_id)
    (let [issue (db/get-issue {:issue_id issue_id})]
      (log/info "issue: " issue)
      (if issue
        (response/ok issue)
        (response/not-found))))

(defn route-handler-global []
  ["/issues"
   {:swagger {:tags ["issues"]}}
   (tags/tags-route-handler (entity/issue) [])
   ["/sync"
    {:swagger {:tags ["sync"]}
     :post    {:summary   "Force synchronize the issues with our git backends. Use with limits"
               :responses {200 {:description "TODO"}
                           403 {:description "TODO"}
                           404 {:description "TODO"}}
               :handler   sync-all}}]
   #_["/:issue_id" {:get {:parameters {:path {:issue_id int?}}
                          :handler    #(get-by-id (get-in % [:path-params :issue_id]))}}]
    ["/:id/feature" {:delete {:summary "Unfeature the issue with the given id."
                          :responses {200 {}
                                      404 {:description "The issue with the specified id does not exist."}}
                          :parameters {:path {:id int?}}
                          :handler #(response/not-implemented)}
                 :post {:summary "Feature the issue with the given id."
                        :responses {200 {}
                                    404 {:description "The issue with the specified id does not exist."}}
                        :parameters {:path {:id int?}}
                        :handler #(response/not-implemented)}}]])

(defn route-handler-per-project []
  ["/issues"
   {:swagger {:tags ["issues"]}}
   ["" {:get {:summary    "Get the issues of a project"
              :responses  {200 {}
                           404 {:description "The project with the specified id does not exist."}}
              :parameters {:path {:id int?}}
              :handler    #(get-project-issues (get-in % [:path-params :id]))}}]])
