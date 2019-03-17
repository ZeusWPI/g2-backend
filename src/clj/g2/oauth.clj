(ns g2.oauth
  (:require [g2.config :refer [env]]
            [clj-http.client :as httpclient]
            [slingshot.slingshot :refer [try+]]
            [clojure.tools.logging :as log]
            [g2.layout :refer [error-page]])
  (:import (org.apache.logging.log4j Level
                                    LogManager)))

;; This is a helper function to change the log level for log4j2. If you use a
;; different logging framework (and subsequently a different bridge for log4j
;; then you'll need to substitute your own logging configuration
(defn change-log-level! [logger-name level]
  (let [ctx (LogManager/getContext false)
        config (.getConfiguration ctx)
        logger-config (.getLoggerConfig config logger-name)]
    (.setLevel logger-config level)
    (.updateLoggers ctx)))


; Inspired by https://leonid.shevtsov.me/post/oauth2-is-easy/

(defn- oauth2-params []
  {:client-id        (env :oauth-consumer-key)
   :client-secret    (env :oauth-consumer-secret)
   :authorize-uri    (env :authorize-uri)
   :redirect-uri     (str (env :app-host) "/oauth/github-callback")
   :access-token-uri (env :access-token-uri)
   })

; To authorize, redirect the user to the sign in / grant page

(defn- authorize-uri
  [client-params                                            ;csrf-token
   ]
  (str
    (:authorize-uri client-params)
    "?"
    (httpclient/generate-query-string {:response_type "code"
                                       :client_id     (:client-id client-params)
                                       :redirect_uri  (:redirect-uri client-params)
                                       :scope "user"} ; github specific
                                      )
    ;"response_type=code"
    ;"&scope="
    ;(url-encode (:scope client-params))
    ;"&state="
    ;(url-encode csrf-token)
    ))

(defn authorize-api-uri
  "let the user authorize access by redirecting to the signin / grant page
 of the used oauth api"
  []
  (log/info "Oauth params: " (oauth2-params))
  (authorize-uri (oauth2-params)))

(defn get-authentication-response
  "Request an access token with the obtained unique code from the grant page"
  [csrf-token {:keys [state code]}]
  (if (or true (= csrf-token state))
    (try
      (do
        (log/info "Requesting access token with code " code)
        (change-log-level! LogManager/ROOT_LOGGER_NAME Level/DEBUG)
        (let [oauth2-params (oauth2-params)
              access-token (httpclient/post (:access-token-uri oauth2-params)
                                            {:form-params {:code          code
                                                           ;:grant_type    "authorization_code" ;needed for zeus auth
                                                           :client_id     (:client-id oauth2-params)
                                                           :client_secret (:client-secret oauth2-params)
                                                           :redirect_uri  (:redirect-uri oauth2-params)}
                                             ;:basic-auth  [(:client-id oauth2-params) (:client-secret oauth2-params)]
                                             :as          :json
                                             :accept      :json
                                             :insecure? true
                                             })]
          ;(println "Access token response:" access-token)
          (:body access-token)))
      (catch Exception e (log/error "Something terrible happened..." e)))
    nil))

(defn get-user-info
  "User info API call"
  [access-token]
  (let [url (str (env :user-api-uri))]
    (-> (httpclient/get url {:oauth-token access-token
                             :as          :json
                             :insecure? true})
        :body)
    ))

; Refresh token when it expires
(defn- refresh-tokens
  "Request a new token pair"
  [refresh-token]
  (try+
    (let [oauth2-params (oauth2-params)
          {{access-token :access_token refresh-token :refresh_token} :body}
          (httpclient/post (:access-token-uri oauth2-params)
                           {:form-params {:grant_type    "refresh_token"
                                          :refresh_token refresh-token}
                            :basic-auth  [(:client-id oauth2-params) (:client-secret oauth2-params)]
                            :as          :json
                            :insecure? true})]
      [access-token refresh-token])
    (catch [:status 401] _ nil)))

(defn get-fresh-tokens
  "Returns current token pair if they have not expired, or a refreshed token pair otherwise"
  [access-token refresh-token]
  (try+
    (and (get-user-info access-token)
         [access-token refresh-token])
    (catch [:status 401] _ (refresh-tokens refresh-token))))

