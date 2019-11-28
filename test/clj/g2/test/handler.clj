(ns g2.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [g2.handler :refer :all]
            [g2.middleware.formats :as formats]
            [muuntaja.core :as m]
            [clojure.pprint :refer [pprint]]
            [mount.core :as mount]
            [clojure.tools.logging :as log]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'g2.config/env
                 #'g2.handler/app)
    (f)))

;; Dummy data
(def project-example {:name "project-name", :description "project-description"})

;; Functions to generate some valid data

(defn get-project
  [pid]
  (app (-> (request :get (str "/project/" pid)))))

(defn create-project
  "Creates a new project"
  ([]
   (create-project project-example))
  ([project-data]
   (app (-> (request :post "/project")
            (json-body project-data)))))

(defn put-project
  "Updates and existing project"
  [pid body]
  (app (-> (request :put (str "/project/" pid))
           (json-body body))))

(defn delete-project
  [pid]
  (app (-> (request :delete (str "/project/" pid)))))

(defn get-generated-key [query-result]
  (-> query-result
      (m/decode-response-body)
      (:new_project_id)))

(defn decode-body [response]
  (m/decode-response-body response))

;; Start of the tests

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response)))))

  (testing "services"

    (testing "project"
      (testing "get-post-delete"
        (let [resp-post (create-project)
              generated_id (get-generated-key resp-post)
              resp-get (get-project generated_id)
              body-get (decode-body resp-get)
              resp-delete (delete-project generated_id)
              resp-get-after-delete (get-project generated_id)]
          (is (= 200 (:status resp-post)))
          (is (= 200 (:status resp-get)))
          (is (= {:project_id generated_id
                  :name "project-name",
                  :description "project-description"
                  :repo_ids nil} body-get))
          (is (= 204 (:status resp-delete)))
          (is (= 404 (:status resp-get-after-delete)))))

      (testing "put"
        (testing "partial"
          (let [resp-post (create-project)
                gen_id (get-generated-key resp-post)
                _ (put-project gen_id {:name "changed-name"})
                body-get (decode-body (get-project gen_id))]
            (is (= (:name body-get) "changed-name"))
            (is (= (:description body-get) (:description project-example)))))
        (testing "full"
          (let [resp-post (create-project)
                gen_id (get-generated-key resp-post)
                _ (put-project gen_id (assoc project-example :name "changed-name" :description "123"))
                body-get (decode-body (get-project gen_id))]
            (is (= (:name body-get) "changed-name"))
            (is (= (:description body-get) "123"))))))

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
