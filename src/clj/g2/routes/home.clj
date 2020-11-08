(ns g2.routes.home
  (:require [g2.layout :as layout]
            [g2.zeus :as zeus-auth]
            [g2.github-auth :as github-auth]
            [g2.db.core :refer [*db*] :as db]
            [g2.oauth :as oauth]
            [g2.routes.repos :as repos]
            [g2.routes.issues :as issues]
            [g2.routes.projects :as projects]
            [g2.routes.labels :as labels]
            [g2.routes.branches :as branches]
            [g2.routes.namedtags :as namedtags]
            [compojure.core :refer [defroutes GET DELETE]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as string]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [g2.routes.pulls :as pulls]))

(defn home-page [request]
  (let [repo-providers (db/get-all-repo-providers)]
    (layout/render request "home.html" {:repo-providers repo-providers
                                        :user           (-> (get-in request [:session :user]))})))

(defroutes home-routes-old
           (GET "/oauth/github" [auth-goal] (github-auth/login-github (keyword auth-goal)))
           (GET "/oauth/github-callback/:auth-goal" [& params :as req] (github-auth/login-github-callback req)))

(defn home-routes []
  [["/" {:get {:handler home-page}}]
   ["/user" {:get {:summary   "Get the current user data from the session."
                   ; :parameters
                   :responses {200 {:body {:user_id int? :name string? :email string? :admin boolean? :last_login string?}}
                               401 {:description "You need to be logged in to get the current user."
                                    :body        {:message string?}}}
                   :handler   (fn [req]
                                (log/debug "session: " (:session req))
                                (if-let [session (:session req)]
                                  (response/ok (-> (get-in session [:user])
                                                   (select-keys '(:user_id, :name, :email, :admin, :last_login))))
                                  (response/unauthorized {:message "User not found. Are you logged in?"})))}}]
   (repos/route-handler-global)
   (projects/route-handler-global)
   (issues/route-handler-global)
   ; not included in the newer spec
   #_(labels/route-handler-global)
   (branches/route-handler-global)
   (pulls/route-handler-global)
   (namedtags/route-handler-global)
   ["/repo-providers"
    {:get {:summary "Get the list of repository providers configured (like for ex. github or gitlab)"
           :handler (fn [_] (response/ok (db/get-all-repo-providers)))}}]
   ["/hooks"
    ["/:id"
     {:delete {:summary    "Delete a git hook."
               :parameters {:path {:id int?}}
               :handler    (fn [req]
                             (log/info "DELETE HOOK: " (pprint req))
                             (response/ok))}}]]             ;TODO test this
   ["/oauth"
    {:swagger {:tags ["oauth"]}}
    ["/github" {:summary "Log into the application using github"
                :get     {:handler (fn [auth-goal] (github-auth/login-github (keyword auth-goal)))}}]
    ["/github-callback/:auth-goal" {:summary "WIP. Authenticate with github in some specific way..."
                                    :get     {:handler (fn [req]
                                                         (github-auth/login-github-callback req))}}]
    ["/zeus"
     ["" {:get {:summary "Log into the application using zeus oauth."
                     :handler zeus-auth/login-zeus}}]
     ["/callback" {:summary "A callback for the oauth login flow."
                         :get     {#_:parameters #_{:query {:code  string?
                                                            :error string?}}
                                   :handler zeus-auth/login-zeus-callback}}]]]])
