(ns g2.git-api
  (:require [clj-http.client :as client]
            [g2.db.core :refer [*db*] :as db]))

(defn get-repositories []
  (db/get-github-repos))

(defn fetch-organisations []
  (:body (client/get "https://api.github.com/user/orgs?access_token=4aad354787e02bf7c3d072ad725983e8731fe097" {:as :json})))

(defn fetch-something [] (client/get "https://api.github.com/user/orgs"))
