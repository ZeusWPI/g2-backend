(ns g2.login
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :as response]))

(defn set-user! [user session redirect-url]
  (log/info "Set user in session: " user)
  (let [new-session (assoc session :user user)]
    (-> (response/found redirect-url)
        (assoc :session new-session))))
