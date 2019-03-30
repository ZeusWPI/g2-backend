(ns g2.routes.home
  (:require [g2.layout :as layout]
            [g2.git-api :as git]
            [g2.github :as gh]
            [g2.zeus :as zeus]
            [g2.db.core :refer [*db*] :as db]
            [g2.oauth :as oauth]
            [compojure.core :refer [defroutes GET DELETE]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]))

(defn home-page [request]
  (let [repo-providers (db/get-all-repo-providers)]
    (layout/render request "home.html" {:repo-providers repo-providers})))

(defn repo-resource [request]
  (response/ok {:repos (git/get-repositories)}))

(defn login-github []
  "Redirect user to github authorize uri"
  (response/found (oauth/authorize-uri (gh/oauth2-params))))

(defn login-github-callback [req-token {:keys [params session]}]
  "Gets the access token from github, connects it as the repo provider"
  (if (:denied params)
    (-> (response/found "/")
        (assoc :flash (:denied true)))
    (let [{:keys [access_token refresh_token]}
          (oauth/get-authentication-response nil req-token (gh/oauth2-params))]
      (let [existing_token (db/get-repo-provider {:name "github"})]
        (log/info "Existing token: " existing_token)
        (if (nil? existing_token)
          (do
            (log/info "Saving new token...")
            (db/create-repo-provider! {:name "github" :access_token access_token}))
          (db/update-repo-provider-access-token! {:name "github" :access_token access_token}))
        (gh/sync-repositories access_token)
        (response/found "/")))))

(defn set-user! [user session redirect-url]
  (log/info "Set user in session: " user)
  (let [new-session (assoc session :user user)]
    (-> (response/found redirect-url)
        (assoc :session new-session))))

(defn login-zeus []
  (response/found (oauth/authorize-uri (zeus/oauth2-params))))

(defn login-zeus-callback [req-token {:keys [params session]}]
  "Retrieves an access token from the zeus adams server for this user.
   If this is a connect, fetch the user and add the github token,
  otherwise create the user first"
  (if (:denied params)
    (-> (response/found "/")
        (assoc :flash (:denied true)))
    (let [{:keys [access_token refresh_token]}
          (oauth/get-authentication-response nil req-token (zeus/oauth2-params))
          xxx (do (println "ACCESS TOKEN: " access_token) 1)
          remote-zeus-user (zeus/get-user-info access_token)
          local-user (db/get-user-on-zeusid {:id_zeus (:id remote-zeus-user)})]
      (if local-user
        (set-user! local-user session "/")
        (try
          (let [new-user {:name (:username remote-zeus-user)
                          :id_zeus (:id remote-zeus-user)}
                generated-key (-> new-user
                                  (db/create-user!))]
            (log/info "Created new user: " generated-key)
            (set-user! (assoc new-user :id (:generated_key generated-key)) session "/"))
          (catch Exception e
            (do
              (log/warn "fetched user" remote-zeus-user "already exists, but was not found")
              (log/warn (:cause (Throwable->map e)))
              (-> (response/found "/")
                  (assoc :flash {:error "An error occurred, please try again."})))))))))

(defroutes home-routes
  (GET "/" request (home-page request))
  (GET "/repositories" request (repo-resource request))
  (GET "/repositories/sync" _ (do (gh/sync-repositories) (response/found "/")))
  (DELETE "/hooks/:id" [id :as req] (log/info "DELETE HOOK: " (pprint req)) (response/found "/"))
  (GET "/docs" []
    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
        (response/header "Content-Type" "text/plain; charset=utf-8")))
  (GET "/oauth/github" [] (login-github))
  (GET "/oauth/github-callback" [& req_token :as req] (login-github-callback req_token req))
  (GET "/oauth/zeus" [] (login-zeus))
  (GET "/oauth/oauth-callback" [& req_token :as req] (login-zeus-callback req_token req)))
