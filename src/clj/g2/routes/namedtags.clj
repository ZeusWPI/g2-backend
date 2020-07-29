(ns g2.routes.namedtags
  (:require
    [g2.db.core :refer [*db*] :as db]
    [g2.routes.tags :as tags]
    [ring.util.http-response :as response]))

(defn route-handler-per-link [link-entity]
  ["/tags"
   ["" {:get {:summary    (str "Get named-tags associated with " link-entity ".")
              :responses  {200 {}
                           404 {:description (str "The " link-entity " with the specified id does not exist.")}}
              :parameters {:path {:id int?}}
              :handler    #(tags/get-tags-linked-with-tag % link-entity "named_tag")}}]]
  )
