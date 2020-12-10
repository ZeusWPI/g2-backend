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
            [g2.services.issues-service :as issues-service]
            [g2.services.projects-service :as projects-service]
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

(defn home-routes []
  [["/" {:get {:handler home-page}}]
   ["/search" {:get {:summary "Search for specific data using a given query."
                     :handler (fn [req]
                                (print "Query params")
                                (println (get req :query-params))
                                (let [q (get-in req [:query-params "q"])
                                      limit (get-in req [:query-params "limit"])
                                      page (get-in req [:query-params "page"])
                                      result
                                      {:issues   (issues-service/get-issues-with q)
                                       :pulls    []
                                       :projects (projects-service/get-projects-with q)
                                       :branches []}]
                                  (println result)
                                  (response/ok result)))}}]
   ["/user" {:get {:summary   "Get the current user data from the session."
                   ; :parameters
                   :responses {200 {:body {:name string? :admin boolean? :avatar string?}}
                               401 {:description "You need to be logged in to get the current user."
                                    :body        {:message string?}}}
                   :handler   (fn [req]
                                (log/debug "session: " (:session req))
                                (if-let [session (:session req)]
                                  (response/ok (-> (get-in session [:user])
                                                   (select-keys '(:name, :admin))
                                                   (assoc :avatar (format "https://eu.ui-avatars.com/api/?background=random?name=%s" (get-in session [:user :name])))))
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
     ["/callback" {:get {#_:parameters #_{:query {:code  string?
                                                  :error string?}}
                         :summary "A callback for the oauth login flow."
                         :handler zeus-auth/login-zeus-callback}}]]
    ["/logout"
     ["" {:get {:summary "Log out the application"
                :handler (fn [req]
                           (log/info "Logout")
                           (response/ok (dissoc :session req)))}}]]]])
