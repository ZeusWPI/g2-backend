(ns g2.zeus
  (:require [g2.config :refer [env]]
            [clj-http.client :as httpclient]))

(defn oauth2-params []
  {:client-id        (env :zeus-oauth-consumer-key)
   :client-secret    (env :zeus-oauth-consumer-secret)
   :authorize-uri    (env :zeus-authorize-uri)
   :redirect-uri     (str (env :app-host) "/oauth/oauth-callback")
   :access-token-uri (env :zeus-access-token-uri)})

(defn get-user-info
  "User info API call"
  [access-token]
  (let [url (str (env :zeus-user-api-uri))]
    (-> (httpclient/get url {:oauth-token access-token
                             :as          :json
                             :insecure? true})
        :body)))

