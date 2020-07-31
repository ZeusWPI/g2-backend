(ns g2.routes.namedtags
  (:require
    [g2.db.core :refer [*db*] :as db]
    [g2.routes.tags :as tags]
    [ring.util.http-response :as response]
    [g2.utils.debugging :refer [log-thread]]
    [clojure.tools.logging :as log]
    [g2.utils.entity :as entity]
    [g2.services.tags-service :as tags-service]
    [g2.services.namedtags-service :as namedtags-service]
    [g2.services.generic-service :as generic-service]))


(defn get-named_tags-linked-with-tag [req link-entity]
  (try
    (response/ok
      (tags-service/assert-get-tags-linked-with-tag req link-entity "named_tags"))
    (catch Exception e
      (let [e-data (ex-data e)
            e-cause (ex-message e)]
        (do
          (log/debug (format "An error occurred: %s" e-cause))
          (log/debug e-data)
          (condp = e-cause
            "not-found" (response/not-found)
            (throw e)))))))

(defn route-handler-global []
  ["/tags"
   {:swagger {:tags ["tag"]}}
   ["" {:get  {:summary   (str "List of tags")
               :responses {200 {}}
               :handler   (fn [_] (do
                                    (log/debug "Fetching named tags")
                                    (->
                                      (entity/get-tags "named_tags")
                                      log-thread
                                      response/ok)))}
        :post {:summary    (str "Create a tag")
               :responses  {200 {}}
               :parameters {:body {:name string? :description string? :color string? #_:type #_string?}}
               :handler    (fn [{body :body-params :as req}]
                             (log/debug "Creating a tag")
                             (let [id (namedtags-service/namedtag-create body)]
                               (response/ok (namedtags-service/namedtag-get id))))}}]
   ["/:id"
    ["" {:delete {:summary    "Delete a named tag"
                  :responses  {204 {}}
                  :parameters {:path {:id int?}}
                  :handler    (fn [{{id :id} :path-params :as req}]
                                (generic-service/delete-entity id "named_tags")
                                (response/no-content))}}]]])

(defn route-handler-per-link [link-entity]
  ["/tags"
   {:swagger {:tags ["tag"]}}
   ["" {:get {:summary    (str "Get tags associated with " link-entity ".")
              :responses  {200 {}
                           404 {:description (str "The " link-entity " with the specified id does not exist.")}}
              :parameters {:path {:id int?}}
              :handler    #(get-named_tags-linked-with-tag % link-entity)}}]])
