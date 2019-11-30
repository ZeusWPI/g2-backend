(ns g2.test.git.github
  (:require [clojure.test :refer :all]
            [g2.git.github :refer [fetch-and-sync-with-local sync-repositories github-endpoints]]
            [clj-http.fake :refer [with-fake-routes]]
            [cheshire.core :as json]
            [g2.db.core :as db]
            [g2.config :refer [env]]))

(deftest test-github-sync
  (testing "fetch-and-sync-with-local"
    (with-fake-routes {"http://some-api/index"
                       (fn [req] {:status 200 :content-type "application/json"
                                  :body   (json/generate-string [{:a "1" :b 2 :c 3 :d 4}
                                                                 {:a "2" :b 3 :c 4}
                                                                 {:a "3" :b 666}])})}
      (fetch-and-sync-with-local "http://some-api/index"
                                 {:a :aa
                                  :b :bb}
                                 :aa
                                 (fn [] [{:aa "another" :bb "one"} ; should be removed
                                         {:aa "1" :bb 2} ; should be kept
                                         {:aa "2" :bb "wrong"} ;should be updated
                                         ])
                                 (fn [new-body] (is (= {:aa "3" :bb 666} new-body)))
                                 (fn [updated-body] (is (= {:aa "2" :bb 3} updated-body))))))
  (testing "Synchronize repositories"
    (testing "All new"
      (with-fake-routes {((get-github-endpoint :repos))
                         (fn [req] {:status 200 :content-type "application/json"
                                    :body (json/generate-string [{:id 123
                                                                  :name "Repo 1"
                                                                  :description "Descr 1"
                                                                  :html_url "http://gh.com/repo/123"
                                                                  :some_other_data 1234}
                                                                 {:id 456
                                                                  :name "Repo 2"
                                                                  :description "Descr 2"
                                                                  :html_url "http://gh.com/repo/456"
                                                                  :some_other_data 1234}])})}
        (sync-repositories)
        (is (= [{:git_id "123"
                 :name "Repo 1"
                 :description "Descr 1"
                 :url "http://gh.com/repo/123"}
                {:git_id "456"
                 :name "Repo 2"
                 :description "Descr 2"
                 :url "http://gh.com/repo/456"}]
               ;; TODO we should have an exact match and don't filter here. Select-keys is fine
               (->> (db/get-repos)
                    (filter (fn [repo] (contains? #{"123" "456"} (:git_id repo))))
                    (map #(select-keys % [:git_id :name :description :url])))))))))
