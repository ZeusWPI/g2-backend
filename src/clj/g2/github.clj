(ns g2.github
  (:require [g2.config :refer [env]]
            [g2.db.core :as db]
            [clj-http.client :as http]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]))

(defn oauth2-params []
  {:client-id        (env :oauth-consumer-key)
   :client-secret    (env :oauth-consumer-secret)
   :authorize-uri    (env :authorize-uri)
   :redirect-uri     (str (env :app-host) "/oauth/github-callback")
   :access-token-uri (env :access-token-uri)
   :scope "admin:org admin:org_hook"})

(def base-url "https://api.github.com")

(defn sync-repositories
  "This will fetch all repositories of the organization and cache basic information in the db"
  ([]
   (sync-repositories (db/get-repo-provider {:name "github"})))
  ([access_token]
   (log/info "Syncing github repositories...")
   (let [response-body
         (:body (http/get (str base-url "/orgs" "/" (env :github-organization) "/repos") {:as :json}))]
     (doseq [full-repo response-body]
       (db/create-github-repo
        (select-keys full-repo [:id :name :description]))))))

; admin:org -> All actions against organization webhooks require the authenticated user to be an admin of the organization being managed.

