(ns g2.test.git.github
  (:require [clojure.test :refer :all]
            [g2.git.github :refer [fetch-and-sync-with-local]]
            [clj-http.fake :refer [with-fake-routes]]
            [cheshire.core :as json]))

(deftest test-sync
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
                                                 (fn [updated-body] (is (= {:aa "2" :bb 3} updated-body)))
                                                 )
                      )
    ))