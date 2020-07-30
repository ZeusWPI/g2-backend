(ns g2.routes.repos
  (:require
    [g2.db.core :refer [*db*] :as db]
    [g2.git.github :as git]
    [ring.util.http-response :as response]
    [g2.utils.projects :as p-util]
    [g2.routes.tags :as tags]
    [conman.core :refer [with-transaction]]
    [clojure.tools.logging :as log]
    [g2.utils.entity :as entity]))

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

; not used for the moment
#_(defn link-repo-to-project [id pid]
    (do
      (log/debug "Link repo" id "to project" pid)
      (p-util/is-project
        pid
        (let [repo (db/get-repo {:repo_id id})]
          (if (nil? repo)
            (response/not-found)
            (with-transaction
              [*db*]
              (do
                (db/link-repo-to-project! {:project_id pid, :repo_id id})
                (response/no-content))))))))

#_(defn unlink-repo-from-project [id pid]
    (response/not-implemented))

(defn repos-get [request]
  (response/ok {:repos (map (fn [repo]
                              (assoc repo :image (str "https://zeus.gent/assets/images/Logos_"
                                                      (:name repo) ".svg")))
                            (db/get-repos))}))

(defn route-handler-global []
  ["/repositories"
   {:swagger {:tags ["repository"]}}
   ["" {:get {:summary "Get the list of code repositories in our backend."
              :handler repos-get}}]
   ["/sync" {:swagger {:tags ["sync"]}
             :post    {:summary "Synchronise the data from all repositories with our database."
                       :handler (fn [_] (git/sync-repositories) (response/ok))}}]
   (tags/tags-route-handler (entity/repository) [])
   #_["/branches"
      [""]
      ["/:branch_id"]]
   #_["/labels"
      [""]
      ["/:label_id"]]])

(defn route-handler-per-project []
  ["/repositories"
   {:swagger {:tags ["repository"]}}
   ["" {:get {:summary    "Get the repositories of a project"
              :responses  {200 {}
                           404 {:description "The project with the specified id does not exist."}}
              :parameters {:path {:id int?}}
              :handler    #(response/ok (tags/assert-get-tags-linked-with-tag % "projects" "repos"))
              }}]])
