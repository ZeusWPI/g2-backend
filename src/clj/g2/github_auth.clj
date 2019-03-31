(ns g2.github-auth
  (:require [g2.db.core :refer [*db*] :as db]
            [ring.util.http-response :as response]
            [g2.oauth :as oauth]
            [g2.git.github :as git]
            [g2.config :refer [env]]
            [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [g2.layout :as layout]))

(defn oauth2-params []
  {:client-id        (env :github-oauth-consumer-key)
   :client-secret    (env :github-oauth-consumer-secret)
   :authorize-uri    (env :github-authorize-uri)
   :redirect-uri     {:auth (str (env :app-host) "/oauth/github-callback/auth")
                      :provider (str (env :app-host) "/oauth/github-callback/provider")}
   :access-token-uri (env :github-access-token-uri)
   :scope "admin:org admin:org_hook"})

(defn login-github [auth-goal]
  "Redirect user to github authorize uri"
  (log/info "Auth-goal: " auth-goal)
  (let [oauth2-params (oauth2-params)]
    (if (#{:auth :provider} auth-goal)
      (->> (assoc oauth2-params :redirect-uri (get-in oauth2-params [:redirect-uri auth-goal]))
           (oauth/authorize-uri)
           (response/found))
      (layout/error-page {:title "Non-supported auth goal"}))))

(defn login-github-callback [{:keys [params session]}]
  "Gets the access token from github, connects it as the repo provider"
  (if (:denied params)
    (-> (response/found "/")
        (assoc :flash (:denied true)))
    (let [{:keys [access_token refresh_token] :as response}
          (oauth/get-authentication-response nil params (oauth2-params))]
      (log/info params)
      (cond
        (:error response)
        (layout/error-page {:status 500
                            :title "Error communicating with github"
                            :message (str (:error_description response)
                                          "  |  "
                                          (:error_uri response))})
        (= (:auth-goal params) "auth") (layout/error-page {:status 200 :title "Not implemented"})
        (= (:auth-goal params) "provider")
        (let [existing_token (db/get-repo-provider {:name "github"})]
          (log/info "Existing token: " existing_token)
          (if (nil? existing_token)
            (do
              (log/info "Saving new token...")
              (db/create-repo-provider! {:name "github" :access_token access_token}))
            (db/update-repo-provider-access-token! {:name "github" :access_token access_token}))
          #_(git/sync-repositories access_token)
          (response/found "/"))))))

