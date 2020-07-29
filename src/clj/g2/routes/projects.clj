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
    [g2.routes.branches :as branches]
    [g2.routes.pulls :as pulls]
    [g2.routes.tags :as tags]
    [g2.utils.entity :as entity]))


#_(defn parse-repo-ids [repo_ids_string]
    (log/debug repo_ids_string)
    (log/debug "type: " (type repo_ids_string))
    (if (nil? repo_ids_string)
      nil
      (map #(Integer/parseInt %)
           (string/split repo_ids_string #","))))

(defn flip [f] (fn [x y & args] (apply f y x args)))



#_(defn linkup-project [project]
    (do
      (log/debug "project: " project)
      (-> project
          (assoc :repositories (str (env :app-host) "/projects/" (:project_id project) "/repositories"))
          (assoc :issues (str (env :app-host) "/projects/" (:project_id project) "/issues"))
          (assoc :pulls (str (env :app-host) "/projects/" (:project_id project) "/pulls"))
          (assoc :branches (str (env :app-host) "/projects/" (:project_id project) "/branches"))
          #_(assoc project :repo_ids (parse-repo-ids (:repo_ids project)))
          )))

(defn projects-get [request]
  (do
    (log/debug "Get Projects")
    (let [projects (db/get-projects)]
      (log/debug "projects: " projects)
      (response/ok projects))))

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
  (do
    (log/debug "Update project" project_id " new values" new-values)
    (with-transaction
      [*db*]
      (let [project (db/get-project {:project_id project_id})]
        (if (nil? project)
          (response/not-found)
          (do
            (-> project
                (comment "(flip merge) because otherwise the new content would be overwritten")
                ((flip merge) new-values)
                (db/update-project!))
            (response/no-content)))))))

(defn project-delete [id]
  (do
    (log/debug "Delete project" id)
    (db/delete-project! {:id id})
    (response/no-content)))

; TODO
(defn project-maintainers [id]
  (do
    (log/debug "Get Maintainers" id)
    (response/ok [])))

; TODO
(defn project-contributors [id]
  (do
    (log/debug "Get Contributors" id)
    (response/ok [])))

; TODO
(defn project-features [id]
  (do
    (log/debug "Get Features" id)
    (response/ok [])))

(defn route-handler-global []
  ["/projects"
   {:swagger {:tags ["project"]}}
   ["" {:get  {:summary   "Get all projects"
               :responses {200 {}}
               :handler   projects-get}
        :post {:summary    "Create a new project"
               :responses  {200 {}
                            422 {:description "The project could not be created with the parameters provided"}}
               :parameters {:body {:name string?, :description string?}}
               :handler    (fn [{{{:keys [name description]} :body} :parameters}] (project-create name description))}}]
   ["/:id"
    ["" {:get    {:summary    "Get a specific project"
                  :responses  {200 {}
                               404 {:description "The project with the specified id does not exist."}}
                  :parameters {:path {:id int?}}
                  :handler    (fn [req] (tags/assert-id-of-entity req "projects"))}
         :delete {:summary    "Delete a specific project"
                  :responses  {200 {}}
                  :parameters {:path {:id int?}}
                  :handler    (fn [req] (let [id (get-in req [:path-params :id])] (project-delete id)))}
         :patch  {:summary    "patch a specific project"
                  :responses  {200 {}
                               404 {:description "The project with the specified id does not exist."}}
                  :parameters {:path {:id int?}
                               :body {:name string?, :description string?, :image string?}}
                  :handler    #(project-edit (get-in % [:path-params :id]) (:body-params %))}}]
    ["/maintainers" {:get {:summary    "Get Maintainers of a specific projects"
                           :responses  {200 {}
                                        404 {:description "The project with the specified id does not exist."}}
                           :parameters {:path {:id int?}}
                           :handler    #(project-maintainers [:path-params :id])}}]
    ["/contributors" {:get {:summary    "Get Contributors of a specific projects"
                            :responses  {200 {}
                                         404 {:description "The project with the specified id does not exist."}}
                            :parameters {:path {:id int?}}
                            :handler    #(project-contributors [:path-params :id])}}]
    ["/features" {:get {:summary    "Get Features of a specific projects"
                        :responses  {200 {}
                                     404 {:description "The project with the specified id does not exist."}}
                        :parameters {:path {:id int?}}
                        :handler    #(project-features [:path-params :id])}}]
    (repos/route-handler-per-project)
    (issues/route-handler-per-project)
    (pulls/route-handler-per-project)
    (labels/route-handler-per-project)
    (branches/route-handler-per-project)
    (tags/tags-operations-route-handler (entity/project) [])]])
