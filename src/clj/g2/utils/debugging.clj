(ns g2.utils.debugging
  (:require
    [clojure.tools.logging :as log]))


(defn log-thread [data]
  (log/debug (format "thread data: ''" (str data)))
  data)
