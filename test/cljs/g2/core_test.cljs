(ns g2.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [pjstadig.humane-test-output]
            [g2.core :as rc]))

(deftest test-home
  (is (= true true)))

