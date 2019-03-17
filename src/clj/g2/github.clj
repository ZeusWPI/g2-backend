(ns g2.github
  (:require [clj-http.client :as client]))

(defn post []
  (client/post
   {:as :json}))

(defn fetch-organisations []
  (:body (client/get "https://api.github.com/user/orgs?access_token=4aad354787e02bf7c3d072ad725983e8731fe097" {:as :json})))

(defn fetch-something []
  (client/get "https://api.github.com/user/orgs"))
