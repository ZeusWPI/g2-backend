(ns g2.zeus
    (:require [g2.config :refer [env]]
      [g2.db.core :refer [*db*] :as db]
      [g2.oauth :as oauth]
      [g2.login :as login]
      [clj-http.client :as httpclient]
      [clojure.tools.logging :as log]
      [java-time :refer [zoned-date-time]]
      [g2.utils.debugging :refer [log-thread]]
      [ring.util.http-response :as response]))

(defn oauth2-params []
      {:client-id        (env :zeus-oauth-consumer-key)
       :client-secret    (env :zeus-oauth-consumer-secret)
       :authorize-uri    (env :zeus-authorize-uri)
       :redirect-uri     (str (env :app-host) "/oauth/zeus/callback")
       :access-token-uri (env :zeus-access-token-uri)})

(defn get-user-info
      "User info API call"
      [access-token]
      (log/debug "Requesting user data from oauth server using the access token.")
      (let [url (str (env :zeus-user-api-uri))
            user (-> (httpclient/get url
                                     {:oauth-token access-token
                                      :as          :json
                                      :accept      :json})
                     :body)]
           (log/debug "Response from the oauth server: " user)
           user))

(defn create-login-user [user session]
      (let [id (-> (db/create-user! user)
                   first
                   :generated_key)]
           (log/debug "Created new user: " id)
           (login/set-user! (assoc user :id id) session "/")))


;;; Login handlers

(defn login-zeus [req]
      (-> (oauth2-params)
          (oauth/authorize-uri)
          (response/found)))

(defn login-zeus-callback
      "Retrieves an access token from the zeus adams server for this user.
       If this is a connect, fetch the user and add the github token,
      otherwise create the user first"
      [{:keys [params session]}]
      (if (:error params)
        (-> (response/unauthorized "User canceled the authentication")
            (assoc :flash (:denied true)))
        (do
          (log/debug "Login request returned from the oauth server.")
          (let [{:keys [access_token refresh_token]} (oauth/get-authentication-response nil params (oauth2-params))
                remote-zeus-user (get-user-info access_token)
                local-user (db/get-user-on-zeusid {:zeus_id (:id remote-zeus-user)})]
               (if local-user
                 (login/set-user! local-user session "/")
                 (try
                   (create-login-user {:name         (:username remote-zeus-user)
                                       :zeus_id      (:id remote-zeus-user)
                                       :access_token access_token}
                                      session)
                   (catch Exception e
                     (do
                       (log/warn "fetched user" remote-zeus-user "already exists, but was not found")
                       (log/warn (:cause (Throwable->map e)))
                       (-> (response/found "/")
                           (assoc :flash {:error "An error occurred, please try again."}))))))))))

