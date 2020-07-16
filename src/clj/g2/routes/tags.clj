(ns g2.routes.tags
  (:require [ring.util.http-response :as response]))

; TODO
(defn get-tags [id]
  (response/ok []))

; TODO
(defn tag-entity-with [id tag]
  (response/ok))

; TODO
(defn untag-entity-with [id tag]
  (response/ok))

(defn tags-route-handler []
  ["tags"
   ["" {:get {:summary "Get tags associated with entity."
              :responses {200 {}
                          404 {:description "The Entity with the specified id does not exist."}}
              :parameters {:path {:id int?}}
              :handler (fn [req] (let [id (get-in req [:path-params :id])] (get-tags id)))}}]
   ["/:tag" {:post {:summary "Tag an entity with a specific tag."
                    :responses {200 {}
                                404 {:description "The Entity or Tag with the specified id does not exist."}}
                    :parameters {:path {:id int?}}
                    :handler (fn [req] (tag-entity-with (get-in req [:path-params :id]) (get-in req [:path-params :tag])))}
             :delete {:summary "Remove specific tag from entity."
                      :responses {200 {}
                                  404 {:description "The Entity or Tag with the specified id does not exist."}}
                      :parameters {:path {:id int?}}
                      :handler (fn [req] (untag-entity-with (get-in req [:path-params :id]) (get-in req [:path-params :tag])))}}]])