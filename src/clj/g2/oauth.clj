(ns g2.oauth
  (:require [g2.config :refer [env]]
            [clj-http.client :as httpclient]
            [slingshot.slingshot :refer [try+]]
            [clojure.tools.logging :as log]
            [g2.layout :refer [error-page]]
            [clojure.pprint :refer [pprint]]
            [luminus.http-server :as http]))

  ;(:import (org.apache.logging.log4j Level
  ;                                   LogManager)))

;; This is a helper function to change the log level for log4j2. If you use a
;; different logging framework (and subsequently a different bridge for log4j
;; then you'll need to substitute your own logging configuration
;(defn change-log-level! [logger-name level]
;  (let [ctx (LogManager/getContext false)
;        config (.getConfiguration ctx)
;        logger-config (.getLoggerConfig config logger-name)]
;    (.setLevel logger-config level)
;    (.updateLoggers ctx)))


; Inspired by https://leonid.shevtsov.me/post/oauth2-is-easy/


; To authorize, redirect the user to the sign in / grant page


(defn authorize-uri
  "Redirect the user to the grant/authorization page"
  [oauth2-params]
  ;(log/info "Oauth params: " oauth2-params)
  (let [state "abc" ;cstf token
        query-map (cond-> {:response_type "code"
                           :client_id     (:client-id oauth2-params)
                           :redirect_uri  (:redirect-uri oauth2-params)}
                    (:scope oauth2-params) (assoc :scope (:scope oauth2-params))
                    state (assoc :state state))
        xx (do (log/info "Authorize uri map: " query-map) 1)
        query-str (httpclient/generate-query-string query-map)
        authorize-uri (str (:authorize-uri oauth2-params)
                           "?"
                           query-str)]
    authorize-uri))

(defn get-authentication-response
  "Request an access token with the obtained unique code from the grant page"
  [csrf-token {:keys [state code]} oauth2-params]
  (if (or true (= csrf-token state))
    (try
      (do
        (log/debug "Requesting access token with code " code)
;        (change-log-level! LogManager/ROOT_LOGGER_NAME Level/DEBUG)
        (let [access-token (httpclient/post (:access-token-uri oauth2-params)
                                            {:form-params {:code          code
                                                           :grant_type    "authorization_code" ;needed for zeus auth ; TODO remove for github?
                                                           :client_id     (:client-id oauth2-params)
                                                           :client_secret (:client-secret oauth2-params)
                                                           :redirect_uri  (:redirect-uri oauth2-params)}
                                             ;:basic-auth  [(:client-id oauth2-params) (:client-secret oauth2-params)]
                                             :as          :json
                                             :accept      :json
                                             :insecure? true})]
          (log/debug "Received access token: "  (:body access-token))
          (:body access-token)))
      (catch Exception e (log/error "Something terrible happened..." e)))
    nil))

;; These refresh functions are not tested or used yet

;(defn- refresh-tokens
;  "Request a new token pair"
;  [refresh-token oauth2-params]
;  (try+
;   (let [{{access-token :access_token refresh-token :refresh_token} :body}
;         (httpclient/post (:access-token-uri oauth2-params)
;                          {:form-params {:grant_type    "refresh_token"
;                                         :refresh_token refresh-token}
;                           :basic-auth  [(:client-id oauth2-params) (:client-secret oauth2-params)]
;                           :as          :json
;                           :insecure? true})]
;     [access-token refresh-token])
;   (catch [:status 401] _ nil)))

;(defn get-fresh-tokens
;  "Returns current token pair if they have not expired, or a refreshed token pair otherwise"
;  [access-token refresh-token]
;  (try+
;   (and (get-user-info access-token)
;        [access-token refresh-token])
;   (catch [:status 401] _ (refresh-tokens refresh-token))))

