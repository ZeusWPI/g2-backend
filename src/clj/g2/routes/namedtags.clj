(ns g2.routes.namedtags
  (:require
    [g2.db.core :refer [*db*] :as db]
    [g2.routes.tags :as tags]
    [ring.util.http-response :as response]
    [g2.utils.debugging :refer [log-thread]]
    [clojure.tools.logging :as log]
    [g2.utils.entity :as entity]))


(defn get-named_tags-linked-with-tag [req link-entity]
  (try
    (response/ok
      (tags/assert-get-tags-linked-with-tag req link-entity "named_tags"))
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
   ["" {:get {:summary   (str "List of tags")
              :responses {200 {}}
              :handler   (fn [_] (do
                                   (log/debug "Fetching named tags")
                                   (->
                                     (entity/get-tags "named_tags")
                                     log-thread
                                     response/ok)))}}]])

(defn route-handler-per-link [link-entity]
  ["/tags"
   ["" {:get {:summary    (str "Get named-tags associated with " link-entity ".")
              :responses  {200 {}
                           404 {:description (str "The " link-entity " with the specified id does not exist.")}}
              :parameters {:path {:id int?}}
              :handler    #(get-named_tags-linked-with-tag % link-entity)}}]])
