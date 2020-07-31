(ns g2.routes.projects
  (:require
    [g2.db.core :refer [*db*] :as db]
    [clojure.tools.logging :as log]
    [clojure.string :as string]
    [clojure.java.jdbc :as jdbc]
    [conman.core :refer [with-transaction]]
    [ring.util.http-response :as response]
    [g2.config :refer [env]]
    [g2.utils.debugging :refer [log-thread]]
    [g2.routes.repos :as repos]
    [g2.routes.issues :as issues]
    [g2.routes.labels :as labels]
    [g2.routes.branches :as branches]
    [g2.routes.namedtags :as namedtags]
    [g2.routes.pulls :as pulls]
    [g2.routes.tags :as tags]
    [g2.services.projects-service :as projects-service]
    [g2.utils.entity :as entity]
    [g2.services.author-service :as author-service]
    [g2.services.issues-service :as issues-service]))


#_(defn parse-repo-ids [repo_ids_string]
    (log/debug repo_ids_string)
    (log/debug "type: " (type repo_ids_string))
    (if (nil? repo_ids_string)
      nil
      (map #(Integer/parseInt %)
           (string/split repo_ids_string #","))))


(defn project-get [{{project-id :id} :path-params :as req}]
  (response/ok (projects-service/project-get project-id)))

(defn projects-get [_]
  (response/ok (projects-service/projects-get)))

(defn project-create [name description]
  (do
    (log/debug "Create project: " name " " description)
    (with-transaction
      [*db*]
      (let [{tag_id :generated_key} (db/create-tag!)]
        (db/create-project! {:tag_id tag_id, :name name, :description description})
        (response/ok (projects-service/project-get tag_id))))))

(defn project-edit [project_id new-values]
  (projects-service/project-edit project_id new-values)
  (response/ok (projects-service/project-get project_id)))

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
    (response/ok
      [{:id     0
        :author (author-service/dummy-author)
        :type   "issue"
        :data   {:issue (first (issues-service/get-project-issues id))}}])))

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
    ["" {:get    {:summary    "Get a project"
                  :responses  {200 {}
                               404 {:description "The project with the specified id does not exist."}}
                  :parameters {:path {:id int?}}
                  :handler    project-get}
         :delete {:summary    "Delete a project"
                  :responses  {200 {}}
                  :parameters {:path {:id int?}}
                  :handler    (fn [req] (let [id (get-in req [:path-params :id])] (project-delete id)))}
         :patch  {:summary    "Modify a project"
                  :responses  {200 {}
                               404 {:description "The project with the specified id does not exist."}}
                  :parameters {:path {:id int?}
                               #_:body #_{:name string?, :description string?}}
                  :handler    #(project-edit (get-in % [:path-params :id]) (:body-params %))}}]
    ["/maintainers" {:get {:summary    "Get Maintainers of a specific projects"
                           :responses  {200 {}
                                        404 {:description "The project with the specified id does not exist."}}
                           :parameters {:path {:id int?}}
                           :handler    #(project-maintainers (get-in % [:path-params :id]))}}]
    ["/contributors" {:get {:summary    "Get Contributors of a specific projects"
                            :responses  {200 {}
                                         404 {:description "The project with the specified id does not exist."}}
                            :parameters {:path {:id int?}}
                            :handler    #(project-contributors (get-in % [:path-params :id]))}}]
    ["/features" {:get {:summary    "Get Features of a specific projects"
                        :responses  {200 {}
                                     404 {:description "The project with the specified id does not exist."}}
                        :parameters {:path {:id int?}}
                        :handler    #(project-features (get-in % [:path-params :id]))}}]
    ["/feature" {:delete {:summary    "Unfeature the project with the given id."
                          :responses  {200 {}
                                       404 {:description "The project with the specified id does not exist."}}
                          :parameters {:path {:id int?}}
                          :handler    (response/not-implemented)}
                 :post   {:summary    "Feature the project with the given id."
                          :responses  {200 {}
                                       404 {:description "The project with the specified id does not exist."}}
                          :parameters {:path {:id int?}}
                          :handler    (response/not-implemented)}}]
    (repos/route-handler-per-project)
    (issues/route-handler-per-project)
    (pulls/route-handler-per-project)
    (branches/route-handler-per-project)
    (namedtags/route-handler-per-link (entity/project))
    (tags/tags-operations-route-handler (entity/project) [])
    ]])
