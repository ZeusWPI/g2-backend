(ns g2.routes.issues
  (:require [g2.db.core :refer [*db*] :as db]
            [g2.git.github :refer [sync-issues]]
            [slingshot.slingshot :refer [try+]]
            [clojure.tools.logging :as log]
            [ring.util.http-response :as response]
            [g2.utils.projects :as p-util]
            [g2.routes.tags :as tags]
            [g2.services.issues-service :as issues-service]
            [g2.services.validator-service :as validator-service]
            [g2.utils.entity :as entity]
            [g2.services.generic-service :as generic-service]))



(defn project-issues [project-id?]
  (do
    (log/debug (format "Get issues for project<%s>" project-id?))
    (if-not [(validator-service/validate-is-project project-id?)]
      (response/not-found)
      (response/ok
        (issues-service/get-project-issues project-id?)))))

(defn sync-all [_]
  (log/debug "Syncing all issues")
  (flush)
  (let [repos (db/get-tags {:table "repos"})]
    (doseq [repo repos]
      (try+
        (sync-issues repo)
        (catch [:status 403] {:keys [body]} (do (log/error "Failed to sync repo_id" (:repo_id repo))
                                                (log/error "Code: 403. This can be due to ratelimiting." body)))
        (catch [:status 404] {:keys [body]} (do (log/error "Failed to sync repo_id" (:repo_id repo) "." body)))))
    (response/no-content)))

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
   ["/sync"
    {:swagger {:tags ["sync"]}
     :post    {:summary   "Force synchronize the issues with our git backends. Use with limits"
               :responses {204 {:description "Sync successful"}
                           403 {:description "TODO"}
                           404 {:description "TODO"}}
               :handler   sync-all}}]
   #_["/:issue_id" {:get {:parameters {:path {:issue_id int?}}
                          :handler    #(get-by-id (get-in % [:path-params :issue_id]))}}]
   (tags/tags-route-handler (entity/issue) [])
   ["/:id/feature" {:delete {:summary    "Unfeature the issue with the given id."
                             :responses  {200 {}
                                          404 {:description "The issue with the specified id does not exist."}}
                             :parameters {:path {:id int?}} ;; TODO check that the entity exists
                             :handler    #(do (generic-service/unfeature-entity (get-in % [:path-params :id]))
                                              (response/no-content))}
                    :post   {:summary    "Feature the issue with the given id."
                             :responses  {200 {}
                                          404 {:description "The issue with the specified id does not exist."}}
                             :parameters {:path {:id int?}} ;; TODO check that the entity exists
                             :handler    #(do (generic-service/feature-entity (get-in % [:path-params :id]))
                                              (response/no-content))}}]])

(defn route-handler-per-project []
  ["/issues"
   {:swagger {:tags ["issues"]}}
   ["" {:get {:summary    "Get the issues of a project"
              :responses  {200 {}
                           404 {:description "The project with the specified id does not exist."}}
              :parameters {:path {:id int?}}
              :handler    #(project-issues (get-in % [:path-params :id])) #_(response/ok (g2.services.generic-service/get-project-entities (get-in % [:path-params :id]) "issues"))}}]])
