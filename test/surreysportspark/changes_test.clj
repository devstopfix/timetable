(ns surreysportspark.changes-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [timetable.changes :as c]
            [surreysportspark.changes :refer :all]))

(def fixture "test/surreysportspark/changes.html")

(defn make-interval [yyyy mm dd h1 m1 h2 m2]
  "Interval for given day between given hours"
  (t/interval
    (t/date-time yyyy mm dd h1 m1)
    (t/date-time yyyy mm dd h2 m2)))

(deftest test-parse-html-for-pool-closures
  (is (=
        [(make-interval 2017 01 29 07 00 19 00)
         (make-interval 2017 02 11 07 00 19 00)
         (make-interval 2017 02 12 07 00 19 00)
         (make-interval 2017 03 23  9 00 12 30)
         (make-interval 2017 03 24 19 00 21 00)
         (make-interval 2017 03 25  7 00 18 00)
         (make-interval 2017 03 26  6 00 18 30)]
        (c/changes-file fixture parse-pool-closures))))
