(ns g2.routes.home
  (:require [g2.layout :as layout]
            [g2.db.core :refer [*db*] :as db]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page [request]
  (layout/render request "home.html"))
(defn example-page [request]
  (let [rslt (db/get-example)]
  (println rslt)
  (layout/render request "exampleview.html" {:rows (db/get-example)})))

(defroutes home-routes
  (GET "/" request (home-page request))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8")))
  (GET "/exampleview" request (example-page request))
  )



