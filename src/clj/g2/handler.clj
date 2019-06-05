(ns g2.handler
  (:require [g2.middleware :as middleware]
            [g2.layout :refer [error-page]]
            [g2.routes.home :refer [home-routes home]]
            [compojure.core :refer [routes wrap-routes]]
            [ring.util.http-response :as response]
            [compojure.route :as route]
            [g2.env :refer [defaults]]
            [mount.core :as mount]

            [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring.coercion :as coercion]
            [reitit.coercion.spec]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.middleware.params :as params]
            [muuntaja.core :as m]
            [clojure.java.io :as io]
            [reitit.dev.pretty :as pretty]
            [reitit.spec :as rs]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop ((or (:stop defaults) identity)))

(def app
  (ring/ring-handler
    (ring/router
      [["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "G2"}
                         :basePath "/"} ;; prefix for all paths
               :handler (swagger/create-swagger-handler)}}]
       (home)
       ["/files"
        {:swagger {:tags ["files"]}}

        ["/upload"
         {:post {:summary "upload a file"
                 :parameters {:multipart {:file multipart/temp-file-part}}
                 :responses {200 {:body {:file multipart/temp-file-part}}}
                 :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                            {:status 200
                             :body {:file file}})}}]

        ["/download"
         {:get {:summary "downloads a file"
                :swagger {:produces ["image/png"]}
                :handler (fn [_]
                           {:status 200
                            :headers {"Content-Type" "image/png"}
                            :body (io/input-stream (io/resource "reitit.png"))})}}]]

       ["/math"
        {:swagger {:tags ["math"]}}

        ["/plus"
         {:get {:summary "plus with spec query parameters"
                :parameters {:query {:x int?, :y int?}}
                :responses {200 {:body {:total int?}}}
                :handler (fn [{{{:keys [x y]} :query} :parameters}]
                           {:status 200
                            :body {:total (+ x y)}})}
          :post {:summary "plus with spec body parameters"
                 :parameters {:body {:x int?, :y int?}}
                 :responses {200 {:body {:total int?}}}
                 :handler (fn [{{{:keys [x y]} :body} :parameters}]
                            {:status 200
                             :body {:total (+ x y)}})}}]]]

      {:data {:coercion reitit.coercion.spec/coercion
              :muuntaja m/instance
              :middleware [;; query-params & form-params
                           parameters/parameters-middleware
                           ;; content-negotiation
                           muuntaja/format-negotiate-middleware
                           ;; encoding response body
                           muuntaja/format-response-middleware
                           ;; exception handling
                           exception/exception-middleware
                           ;; decoding request body
                           muuntaja/format-request-middleware
                           ;; coercing response bodys
                           coercion/coerce-response-middleware
                           ;; coercing request parameters
                           coercion/coerce-request-middleware
                           ;; multipart
                           multipart/multipart-middleware]}
       :exception pretty/exception
       :validate rs/validate})
    (ring/routes
      (swagger-ui/create-swagger-ui-handler {:path "/swagger-ui"})
      (ring/create-resource-handler {:path "/"})
      (ring/create-default-handler))))

;(middleware/wrap-base
;  (routes
;    (-> #'home-routes
;        (wrap-routes middleware/wrap-csrf)
;        (wrap-routes middleware/wrap-formats))
;    (route/not-found
;      (:body
;        (error-page {:status 404
;                     :title "page not found"})))))
