(ns g2.routes.tags
  (:require [ring.util.http-response :as response]
            [clojure.string :as string]))

(defn assert-entity [req query f]
  (let [entity (query (get-in req [:path-params :id]))]
    (if (nil? entity)
      (response/not-found)
      (f entity))))

(defn get-entity [entity]
  (response/ok entity))

; TODO
(defn get-tags [entity]
  (response/ok []))

; TODO
(defn tag-entity-with [entity tag]
  (response/ok))

; TODO
(defn untag-entity-with [entity tag]
  (response/ok))

(defn tags-operations-route-handler [^String what identity-query]
  ["/tags"
   ["" {:get {:summary (str "Get tags associated with " what ".")
              :responses {200 {}
                          404 {:description (str "The " what " with the specified id does not exist.")}}
              :parameters {:path {:id int?}}
              :handler #(assert-entity % identity-query get-tags)}}]
   ["/:tag" {:post   {:summary    (str "Tag an " what " with a specific tag.")
                      :responses  {200 {}
                                   404 {:description (str "The " what " or Tag with the specified id does not exist.")}}
                      :parameters {:path {:id int?}}
                      :handler    #(assert-entity % identity-query (fn [x] (tag-entity-with x (get-in % [:path-params :tag]))))}
             :delete {:summary    (str "Remove specific tag from " what ".")
                      :responses  {200 {}
                                   404 {:description (str "The " what " or Tag with the specified id does not exist.")}}
                      :parameters {:path {:id int?}}
                      :handler    #(assert-entity % identity-query (fn [x] (untag-entity-with x (get-in % [:path-params :tag]))))}}]])

(defn tags-route-handler [^String what identity-query]
  ["/:id"
   ["" {:get {:summary (str "Get a " what " by id")
              :responses {200 {}
                          404 {:description (str "The " what " with the specified id does not exist.")}}
              :parameters {:path {:id int?}}
              :handler #(assert-entity % identity-query get-entity)}}]
   (tags-operations-route-handler what identity-query)])