(ns g2.routes.pulls
  (:require [ring.util.http-response :as response]))

; TODO
(defn get-all-for-project [id]
  (response/ok []))

(defn route-handler-per-project []
  ["/pulls" {:get {:summary    "Get the pulls of a project"
                    :responses  {200 {}
                                 404 {:description "The project with the specified id does not exist."}}
                    :parameters {:path {:id int?}}
                    :handler    #(get-all-for-project (get-in % [:path-params :id]))}}])