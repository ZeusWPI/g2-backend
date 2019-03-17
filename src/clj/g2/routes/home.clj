(ns g2.routes.home
  (:require [g2.layout :as layout]
            [g2.github :refer [fetch-something]]
            [g2.db.core :as db]
            [g2.oauth :as oauth]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

(defn home-page [request]
  (layout/render request "home.html" {:organisations [1 2 3]}))

(defn login-github []
  "Redirect user to github authorize uri"
  (response/found (oauth/authorize-api-uri)))

(defn login-github-callback [req-token {:keys [params session]}]
  "Gets the access token from github.
   If this is a connect, fetch the user and add the github token,
   otherwise create the user first"
  (if (:denied params)
    (-> (response/found "/")
        (assoc :flash (:denied true)))
    (let [{:keys [access_token refresh_token]} (oauth/get-authentication-response nil req-token)]
      (log/info "Fetched access-id: " access_token)
      (response/found "/"))))



(defroutes home-routes
  (GET "/" request (home-page request))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8")))
  (GET "/oauth/github" [] (login-github))
  (GET "/oauth/github-callback" [& req_token :as req] (login-github-callback req_token req)))
