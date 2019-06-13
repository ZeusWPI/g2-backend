(ns g2.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [g2.handler :refer :all]
            [g2.middleware.formats :as formats]
            [muuntaja.core :as m]
            [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'g2.config/env
                 #'g2.handler/app)
    (f)))

(deftest test-app
  #_(testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  #_(testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response)))))
  (testing "services"

    (testing "success"
      (let [response (app (-> (request :post "/project")
                              (json-body {:name "project-name", :description "project-description"})))]
        (is (= 200 (:status response)))
        #_(is (= {:total 16} (m/decode-response-body response)))))

    (testing "parameter coercion error"
      (let [response (app (-> (request :post "/project")
                              (json-body {:name "project-name", :description 1})))]
        (is (= 400 (:status response)))))

    #_(testing "response coercion error"
      (let [response (app (-> (request :post "/api/math/plus")
                              (json-body {:x -10, :y 6})))]
        (is (= 500 (:status response)))))

    #_(testing "content negotiation"
      (let [response (app (-> (request :post "/api/math/plus")
                              (body (pr-str {:x 10, :y 6}))
                              (content-type "application/edn")
                              (header "accept" "application/transit+json")))]
        (is (= 200 (:status response)))
        (is (= {:total 16} (m/decode-response-body response)))))))
