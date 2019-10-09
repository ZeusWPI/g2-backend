(ns g2.test.db.core
  (:require [g2.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [g2.config :refer [env]]
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
    (let [insert_id (db/create-user!
                     t-conn
                     {:name "Foo"
                      :zeus_id 10
                      :access_token "abcd"})
          id (:generated_key insert_id)
          result (db/get-user t-conn {:id id})]
      (is (= {:id id
              :name "Foo"
              :zeus_id 10
              :email nil
              :admin nil
              :last_login nil
              :access_token "abcd"}
             result)))))

(deftest test-repositories
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [insert_id (db/create-repo!
                     t-conn
                     {:git_id 1234
                      :name "g2"
                      :description "The best project"
                      :url "https://github.com/zeuswpi/g2"})
          id (:generated_key insert_id)
          result (db/get-repo t-conn {:repo_id id})]
      (is (= {:repo_id id
              :git_id 1234
              :name "g2"
              :description "The best project"
              :url "https://github.com/zeuswpi/g2"
              :project_id nil}
             result)))))

(deftest test-projects
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [insert_id (db/create-project!
                     t-conn
                     {:name "test name"
                      :description "test description"})
          project_id (:generated_key insert_id)
          result (db/get-project t-conn {:project_id project_id})]
      (is (= {:project_id project_id
              :name "test name"
              :description "test description"
              :repo_ids nil}
             result)))))
