(ns g2.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [g2.core-test]))

(doo-tests 'g2.core-test)

