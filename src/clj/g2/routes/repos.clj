(ns g2.routes.repos
  (:require
   [g2.db.core :refer [*db*] :as db]
   [g2.git.github :as git]
   [ring.util.http-response :as response]
   [g2.utils.projects :as p-util]))

(defn convert-db-to-api-object
  [db-repo]
  (-> db-repo
      (dissoc :default-tags)))

(defn get-project-repositories
  [project_id]
  (p-util/is-project
    project_id
    (response/ok (-> (db/get-project-repos {:project_id project_id})
                     convert-db-to-api-object))))

(defn repo-get
  "Fetch 1 repo"
  [repo_id]
  (let [repo (db/get-repo {:repo_id repo_id})]
    (if-not (nil? repo)
      (response/ok repo)
      (response/not-found {:msg "Repository not found"}))))

(defn link-repo-to-project [id pid]
  ;; TODO check that the project exists
  ;; TODO check that the repo exists
  (do
    (db/link-repo-to-project! {:project_id pid, :repo_id id})
    (response/no-content)))

(defn repos-get [request]
  (response/ok {:repos (map (fn [repo]
                              (assoc repo :image (str "https://zeus.gent/assets/images/Logos_"
                                                      (:name repo) ".svg")))
                            (db/get-repos))}))

(defn route-handler-global []
  ["/repository"
   {:swagger {:tags ["repository"]}}
   ["" {:get {:summary "Get the list of code repositories in our backend."
              :handler repos-get}}]
   ["/sync" {:swagger {:tags ["sync"]}
             :post    {:summary "Synchronise the data from all repositories with our database."
                       :handler (fn [_] (git/sync-repositories) (response/ok))}}]
   ["/:id"
    ["" {:get {:summary    "Get a specific repository."
               :responses  {200 {}
                            404 {:description "The repository with the specified id does not exist."}}
               :parameters {:path {:id int?}}
               :handler    (fn [req] (let [id (get-in req [:path-params :id])] (repo-get id)))}}]
    ["/link/:pid" {:put {:summary    "Connect a repository to a project"
                         :parameters {:path {:pid int?, :id int?}}
                         :handler    (fn [{{:keys [id pid]} :path-params}] (link-repo-to-project id pid))}}]
    ["/branches"
     [""]
     ["/:branch_id"]]
    ["/labels"
     [""]
     ["/:label_id"]]]])

(defn route-handler-per-project []
  ["/repositories"
   {:swagger {:tags ["repository"]}}
   ["" {:get {:summary    "Get the repositories of a project"
              :responses  {200 {}
                           404 {:description "The project with the specified id does not exist."}}
              :parameters {:path {:id int?}}
              :handler    #(get-project-repositories (get-in % [:path-params :id]))}}]])
