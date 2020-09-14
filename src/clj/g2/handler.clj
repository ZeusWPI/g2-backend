(ns g2.handler
  (:require [g2.middleware :as middleware]
            [g2.middleware.formats :as formats]
            #_[g2.middleware.exception :as exception]
            [g2.layout :refer [error-page]]
            [g2.routes.home :refer [home-routes]]
            [g2.env :refer [defaults]]
            [compojure.core :refer [routes wrap-routes]]
            [mount.core :as mount]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.coercion.spec :as spec-coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.ring.middleware.exception :as exception]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [ring.middleware.content-type :refer [wrap-content-type]]))

(defn app-routes []
  [["" {:no-doc  true
        :swagger {:info {:title "G2"}}}
    ["/swagger.json"
     {:get {:handler (swagger/create-swagger-handler)}}]
    ["/api-docs/*" {:get (swagger-ui/create-swagger-ui-handler
                           {:url "/swagger.json"})}]]
   (home-routes)
   #_[["/files"
       {:swagger {:tags ["files"]}}

       ["/upload"
        {:post {:summary    "upload a file"
                :parameters {:multipart {:file multipart/temp-file-part}}
                :responses  {200 {:body {:file multipart/temp-file-part}}}
                :handler    (fn [{{{:keys [file]} :multipart} :parameters}]
                              {:status 200
                               :body   {:file file}})}}]

       ["/download"
        {:get {:summary "downloads a file"
               :swagger {:produces ["image/png"]}
               :handler (fn [_]
                          {:status  200
                           :headers {"Content-Type" "image/png"}
                           :body    (io/input-stream (io/resource "reitit.png"))})}}]]

      ["/math"
       {:swagger {:tags ["math"]}}

       ["/plus"
        {:get  {:summary    "plus with spec query parameters"
                :parameters {:query {:x int?, :y int?}}
                :responses  {200 {:body {:total int?}}}
                :handler    (fn [{{{:keys [x y]} :query} :parameters}]
                              {:status 200
                               :body   {:total (+ x y)}})}
         :post {:summary    "plus with spec body parameters"
                :parameters {:body {:x int?, :y int?}}
                :responses  {200 {:body {:total int?}}}
                :handler    (fn [{{{:keys [x y]} :body} :parameters}]
                              {:status 200
                               :body   {:total (+ x y)}})}}]]]])


(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop ((or (:stop defaults) identity)))

(mount/defstate app
                :start
                (middleware/wrap-base
                  (ring/ring-handler
                    (ring/router
                      [""
                       {:coercion   spec-coercion/coercion
                        :muuntaja   formats/instance
                        :middleware [;; query-params & form-params
                                     parameters/parameters-middleware
                                     ;; multipart
                                     multipart/multipart-middleware
                                     ;; content-negotiation
                                     muuntaja/format-negotiate-middleware
                                     ;; decoding request body
                                     muuntaja/format-request-middleware
                                     ;; coercing request parameters
                                     coercion/coerce-request-middleware
                                     ;; encoding response body
                                     muuntaja/format-response-middleware
                                     ;; coercing response bodies
                                     coercion/coerce-response-middleware
                                     ;; exception handling
                                     exception/exception-middleware
                                     ]}
                       #_["/" {:get {:handler (constantly {:status 301 :headers {"Location" "/api-docs/index.html"}})}}]
                       (app-routes)]
                      #_{:conflicts (fn [conflicts]
                                      (println (str "Conflict: " conflicts)))})
                    (ring/routes
                      #_(ring/create-resource-handler
                          {:path "/"})
                      #_wrap-content-type
                      (ring/create-default-handler)))))

