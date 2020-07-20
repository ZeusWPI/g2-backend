(ns g2.routes.tags
  (:require [ring.util.http-response :as response]
            [g2.utils.entity :as entity])
  (:import (java.util List)))

(defn assert-id-of-entity [req entity f]
  (let [db-object ((entity/-get entity) (get-in req [:path-params :id]))]
    (if (nil? db-object)
      (response/not-found)
      (f db-object))))

(defn get-entity [db-object]
  (response/ok db-object))

; TODO write a db-querry to fetch the named-tags of an entity
(defn get-tags [db-object]
  (response/ok []))

; TODO implement this using the allowed-links list, these are tho only entities that are allowed to link with this entity
(defn tag-entity-with [db-object tag]
  (response/ok))

; TODO
(defn untag-entity-with [db-object tag]
  (response/ok))

(defn tags-operations-route-handler [entity ^List allowed-links]
  (let [what (entity/-name entity)]
    ["/tags"
     ["" {:get {:summary    (str "Get tags associated with " what ".")
                :responses  {200 {}
                             404 {:description (str "The " what " with the specified id does not exist.")}}
                :parameters {:path {:id int?}}
                :handler    #(assert-id-of-entity % entity get-tags)}}]
     ["/:tag" {:post   {:summary    (str "Tag an " what " with a specific tag.")
                        :responses  {200 {}
                                     404 {:description (str "The " what " or Tag with the specified id does not exist.")}}
                        :parameters {:path {:id int?}}
                        :handler    #(assert-id-of-entity % entity (fn [x] (tag-entity-with x (get-in % [:path-params :tag]))))}
               :delete {:summary    (str "Remove specific tag from " what ".")
                        :responses  {200 {}
                                     404 {:description (str "The " what " or Tag with the specified id does not exist.")}}
                        :parameters {:path {:id int?}}
                        :handler    #(assert-id-of-entity % entity (fn [x] (untag-entity-with x (get-in % [:path-params :tag]))))}}]]))

(defn tags-route-handler [entity allowed-links]
  (let [what (entity/-name entity)]
    ["/:id"
     ["" {:get {:summary    (str "Get a " what " by id")
                :responses  {200 {}
                             404 {:description (str "The " what " with the specified id does not exist.")}}
                :parameters {:path {:id int?}}
                :handler    #(assert-id-of-entity % entity get-entity)}}]
     (tags-operations-route-handler entity allowed-links)]))