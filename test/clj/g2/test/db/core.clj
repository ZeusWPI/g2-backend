(ns g2.test.db.core
  (:require [g2.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [g2.config :refer [env]]
            [java-time :refer [local-date-time truncate-to instant]]
            [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
     #'g2.config/env
     #'g2.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-users
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [last-login  (java-time/truncate-to (local-date-time) :seconds)
          id (-> (db/create-user!
                     t-conn
                     {:name "Foo"
                      :email "123"
                      :admin false
                      :last_login last-login})
                        first
                        :generated_key)
          result (db/get-user t-conn {:user_id id})]
      (is (= {:user_id id
              :name "Foo"
              :email "123"
              :admin false
              :zeus_id nil
              :last_login last-login}
             result)))))

(deftest test-repositories
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [id (-> (db/create-repo!
                     t-conn
                     {:git_id "1234"
                      :name "g2"
                      :description "The best project"
                      :url "https://github.com/zeuswpi/g2"})
                        first
                        :generated_key)
          result (db/get-repo t-conn {:repo_id id})]
      (is (= {:repo_id id
              :git_id "1234"
              :repo_type nil
              :name "g2"
              :description "The best project"
              :url "https://github.com/zeuswpi/g2"
              :project_id nil}
             result
             )))))

(deftest test-projects
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [project_id (-> (db/create-project!
                     t-conn
                     {:name "test name"
                      :description "test description"})
                 first
                 :generated_key)
          result (db/get-project t-conn {:project_id project_id})]
      (is (= {:project_id project_id
              :name "test name"
              :description "test description"
              :image_url nil
              :repo_ids nil}
             result)))))
