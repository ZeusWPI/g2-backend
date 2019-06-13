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
    (is (= 1 (db/create-user!
              t-conn
              {:name "Foo"
               :zeus_id 10
               :access_token "abcd"})))
    (is (= {:id 1
            :name "Foo"
            :zeus_id 10
            :email nil
            :admin nil
            :last_login nil
            :access_token "abcd"}
           (db/get-user t-conn {:id 1})))))

(deftest test-repositories
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (is (= 1 (db/create-repo!
              {:id 1234
               :name "g2"
               :description "The best project"
               :url "https://github.com/zeuswpi/g2"})))
    (is (= {:repo_id 1
            :git_id 1234
            :name "g2"
            :description "The best project"
            :url "https://github.com/zeuswpi/g2"
            :project_id nil}
           (db/get-repo t-conn {:id 1})))))
