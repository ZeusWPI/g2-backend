(ns g2.routes.projects
  (:require
    [g2.db.core :refer [*db*] :as db]
    [clojure.tools.logging :as log]
    [clojure.string :as string]
    [clojure.java.jdbc :as jdbc]
    [conman.core :refer [with-transaction]]
    [ring.util.http-response :as response]
    [g2.config :refer [env]]
    [g2.routes.repos :as repos]
    [g2.routes.issues :as issues]
    [g2.routes.labels :as labels]
    [g2.routes.branches :as branches]))


(defn parse-repo-ids [repo_ids_string]
  (log/debug repo_ids_string)
  (log/debug "type: " (type repo_ids_string))
  (if (nil? repo_ids_string)
    nil
    (map #(Integer/parseInt %)
         (string/split repo_ids_string #","))))

(defn projects-get [request]
  (let [projects (db/get-projects)]
    (log/debug "projects: " projects)
    (let [n (map (fn [project]
                   (log/debug "project: " project)
                   (-> project
                       (assoc :repositories (str (env :app-host) "/projects/" (:project_id project) "/repositories"))
                       (assoc :issues (str (env :app-host) "/projects/" (:project_id project) "/issues"))
                       (assoc :pulls (str (env :app-host) "/projects/" (:project_id project) "/pulls"))
                       (assoc :branches (str (env :app-host) "/projects/" (:project_id project) "/branches"))
                       )

                   #_(assoc project :repo_ids (parse-repo-ids (:repo_ids project))))
                 projects)]
      (log/debug "n-projects: " n)
      (response/ok n))))

(defn project-get [project_id]
  (log/debug "Get project" project_id)
  (let [project (db/get-project {:project_id project_id})]
    (if-not (nil? project)
      (response/ok project)
      (response/not-found))))

(defn project-create [name description]
  (do
    (log/debug "Create project: " name " " description)
    (with-transaction
      [*db*]
      (let [{tag_id :generated_key} (db/create-tag!)]
        (db/create-project! {:tag_id tag_id, :name name, :description description})
        (response/ok {:new_project_id tag_id})))
    ))

(defn project-edit [project_id new-values]
  (as-> (db/get-project {:project_id project_id}) v
        (if (nil? v)
          (response/not-found)
          (do
            (-> v
                (merge new-values)
                (db/update-project!))
            (response/no-content)))))

(defn project-delete [id]
  (do
    (db/delete-project! {:id id})
    (log/debug "Delete project" id)
    (response/no-content)))


(defn route-handler-global []
  ["/projects"
   {:swagger {:tags ["project"]}}
   ["" {:get  {:summary "Get all projects"
               :handler projects-get}
        :post {:summary    "Create a new project"
               :parameters {:body {:name string?, :description string?}}
               :handler    (fn [{{{:keys [name description]} :body} :parameters}] (project-create name description))}}]

   ["/:id"
    ["" {:get    {:summary    "Get a specific project"
                  :responses  {200 {}
                               404 {:description "The project with the specified id does not exist."}}
                  :parameters {:path {:id int?}}
                  :handler    (fn [req] (let [id (get-in req [:path-params :id])] (project-get id)))}
         :delete {:summary    "Delete a project"
                  :parameters {:path {:id int?}}
                  :handler    (fn [req] (let [id (get-in req [:path-params :id])] (project-delete id)))}
         :put    {:parameters {:path {:id int?}
                               :body {}}
                  :handler    (fn [req] (project-edit (get-in req [:path-params :id]) (:body-params req)))}}]
    (repos/route-handler-per-project)
    (issues/route-handler-per-project)
    ;(pulls/route-handler-per-project) ; TODO
    ;(labels/route-handler-per-project)
    (branches/route-handler-per-project)]])
