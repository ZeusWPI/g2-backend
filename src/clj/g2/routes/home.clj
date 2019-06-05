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
            [clojure.pprint :refer [pprint]]))

(defn home-page [request]
  (let [repo-providers (db/get-all-repo-providers)]
    (layout/render request "home.html" {:repo-providers repo-providers
                                        :user (-> (get-in request [:session :user]))})))

(defn repo-resource [request]
  (response/ok {:repos (db/get-github-repos)}))

(defroutes home-routes
  (GET "/" request (home-page request))
  (GET "/repositories" request (repo-resource request))
  (GET "/repositories/sync" _ (do (git/sync-repositories) (response/found "/")))
  (DELETE "/hooks/:id" [id :as req] (log/info "DELETE HOOK: " (pprint req)) (response/found "/"))
  (GET "/docs" []
    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
        (response/header "Content-Type" "text/plain; charset=utf-8")))
  (GET "/oauth/github" [auth-goal] (github-auth/login-github (keyword auth-goal)))
  (GET "/oauth/github-callback/:auth-goal" [& params :as req] (github-auth/login-github-callback req))
  (GET "/oauth/zeus" [] (zeus-auth/login-zeus))
  (GET "/oauth/oauth-callback" [& params :as req] (zeus-auth/login-zeus-callback params req)))
