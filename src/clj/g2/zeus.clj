(ns g2.zeus
  (:require [g2.config :refer [env]]
            [clj-http.client :as httpclient]))

(defn get-user-info
  "User info API call"
  [access-token]
  (let [url (str (env :user-api-uri))]
    (-> (httpclient/get url {:oauth-token access-token
                             :as          :json
                             :insecure? true})
        :body)))

