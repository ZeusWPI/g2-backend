(ns g2.routes.pulls
  (:require [ring.util.http-response :as response]
            [g2.db.core :refer [*db*] :as db]
            [g2.routes.tags :as tags]
            [g2.utils.entity :as entity]))

(defn get-pull [id]
  (response/ok {}))

; TODO
(defn get-all-for-project [id]
  (response/ok []))

; TODO implement pulls in db
#_(defn route-handler-global []
  ["/pulls"
   (tags/tags-route-handler "Pull request" db/get-pulls)])

(defn route-handler-global []
  ["/pulls"
   {:swagger {:tags ["pulls"]}}
   (tags/tags-route-handler (entity/pull) [])
   ["/:id"
    ["/feature" {:delete {:summary "Unfeature the pull request with the given id."
                          :responses {200 {}
                                      404 {:description "The pull request with the specified id does not exist."}}
                          :parameters {:path {:id int?}}
                          :handler #(response/not-implemented)}
                 :post {:summary "Feature the pull request with the given id."
                        :responses {200 {}
                                    404 {:description "The pull request with the specified id does not exist."}}
                        :parameters {:path {:id int?}}
                        :handler #(response/not-implemented)}}]]
   ])

(defn route-handler-per-project []
  ["/pulls" {:get {:summary    "Get the pulls of a project"
                    :responses  {200 {}
                                 404 {:description "The project with the specified id does not exist."}}
                    :parameters {:path {:id int?}}
                    :handler    #(get-all-for-project (get-in % [:path-params :id]))}}])