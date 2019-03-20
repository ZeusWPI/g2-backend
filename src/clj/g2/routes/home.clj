(ns g2.routes.home
  (:require [g2.layout :as layout]
            [g2.db.core :refer [*db*] :as db]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page [request]
  (layout/render request "home.html"))

(defn project-list [request]
  (layout/render request "project_list.html" {:list-of-projects  (db/get-projects)}))


(defn fetch []
  (println (db/get-projects))
  (response/found "/")
  )

(defroutes home-routes
           (GET "/" request (home-page request))
           (GET "/projects" request (project-list request))
           (GET "/docs" []
             (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                 (response/header "Content-Type" "text/plain; charset=utf-8"))))