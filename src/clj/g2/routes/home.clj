(ns g2.routes.home
  (:require [g2.layout :as layout]
            [g2.git.github :as git]
            [g2.zeus :as zeus-auth]
            [g2.github-auth :as github-auth]
            [g2.db.core :refer [*db*] :as db]
            [g2.oauth :as oauth]
            [compojure.core :refer [defroutes GET DELETE]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as string]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]))

(defn home-page [request]
  (let [repo-providers (db/get-all-repo-providers)]
    (layout/render request "home.html" {:repo-providers repo-providers
                                        :user (-> (get-in request [:session :user]))})))

(defn repo-resource [request]
  (response/ok {:repos (map (fn [repo]
                              (assoc repo :image (str "https://zeus.gent/assets/images/Logos_"
                                                      (:name repo) ".svg")))
                            (db/get-repos))}))

(defn repo-get
  "Fetch 1 repo"
  [repo_id]
  (let [repo (db/get-repo {:repo_id repo_id})]
    (if-not (nil? repo)
      (response/ok repo)
      (response/not-found {:msg "Repository not found"}))))

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
    (let [n (map (fn [project] (log/debug "project: " project) (assoc project :repo_ids (parse-repo-ids (:repo_ids project)))) projects)]
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
    (let [insert_id (db/create-project! {:name name, :description description})]
      (response/ok {:new_project_id (:generated_key insert_id)}))))

(defn project-delete [id]
  (do
    (db/delete-project! {:id id})
    (log/debug "Delete project" id)
    (response/no-content)))

(defn link-repo-to-project [id pid]
  (do
    (db/link-repo-to-project! {:project_id pid, :repo_id id})
    (response/no-content)))

(defroutes home-routes-old
  (GET "/oauth/github" [auth-goal] (github-auth/login-github (keyword auth-goal)))
  (GET "/oauth/github-callback/:auth-goal" [& params :as req] (github-auth/login-github-callback req)))

(defn home-routes []
  [["/" {:get {:handler home-page}}]
   ["/user" {:get {:handler (fn [req] (log/info "session: " (:session req)) (response/ok (get-in req [:session :user])))}}]
   ["/repository"
    ["" {:get {:handler repo-resource}}]
    ["/sync" {:post {:summary "Don't spam this endpoint as we will be blocked by Github. Ratelimitting on the backend will be implemented soon."
                     :handler (fn [_] (git/sync-repositories))}}]
    ["/:id"
     ["" {:get {:parameters {:path {:id int?}}
                :handler (fn [req] (let [id (get-in req [:path-params :id])] (repo-get id)))}}]
     ["/link/:pid" {:put {:parameters {:path {:pid int?, :id int?}}
                          :handler (fn [{{:keys [id pid]} :path-params}] (link-repo-to-project id pid))}}]]]
   ["/project"
    ["" {:get {:handler projects-get}
         :post {:parameters {:body {:name string?, :description string?}}
                :handler (fn [{{{:keys [name description]} :body} :parameters}] (project-create name description))}}]
    ["/:id" {:get {:parameters {:path {:id int?}}
                   :handler (fn [req] (let [id (get-in req [:path-params :id])] (project-get id)))}
             :delete {:parameters {:path {:id int?}}
                      :handler (fn [req] (let [id (get-in req [:path-params :id])] (project-delete id)))}}]]
   ["/repo-providers"
    {:get {:handler (fn [_] (response/ok (db/get-all-repo-providers)))}}]
   ["/hooks"
    ["/:id"
     {:delete {:summary "delete a git hook"
               :parameters {:path {:id int?}}
               :handler (fn [req]
                          (log/info "DELETE HOOK: " (pprint req))
                          (response/ok))}}]] ;TODO test this
   ["/oauth"
    ["/github" {:get {:handler (fn [auth-goal] (github-auth/login-github (keyword auth-goal)))}}]
    ["/github-callback/:auth-goal" {:get {:handler (fn [req]
                                                     (github-auth/login-github-callback req))}}]
    ["/zeus" {:get {:handler zeus-auth/login-zeus}}]
    ["/oauth-callback" {:get {#_:parameters #_{:query {:code string?
                                                       :error string?}}
                              :handler zeus-auth/login-zeus-callback}}]]])
