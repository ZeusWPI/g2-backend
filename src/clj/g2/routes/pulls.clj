(ns g2.routes.pulls
  (:require [ring.util.http-response :as response]
            [g2.routes.tags :as tags]))

(defn get-pull [id]
  (response/ok {}))

; TODO
(defn get-all-for-project [id]
  (response/ok []))

(defn route-handler-global []
  ["/pulls"
   ["/:id"
    ["" {:get {:summary "Get a pull request by id"
               :responses {200 {}
                           404 {:description "The pull request with the specified id does not exist."}}
               :parameters {:path {:id int?}}
               :handler #(get-pull [:path-params :id])}}]]
   (tags/tags-route-handler)])

(defn route-handler-per-project []
  ["/pulls" {:get {:summary    "Get the pulls of a project"
                    :responses  {200 {}
                                 404 {:description "The project with the specified id does not exist."}}
                    :parameters {:path {:id int?}}
                    :handler    #(get-all-for-project (get-in % [:path-params :id]))}}])