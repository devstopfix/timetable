(ns surreysportspark.pool
  (:require [clj-time.core :as t]
            [clj-time.predicates :as pr]
            [timetable.core :as tt]))

; All our Events are in London (GMT or BST)
(def tz-london (t/time-zone-for-id "Europe/London"))

; We publish calenders to S3 bucket
(def bucket-name (System/getenv "S3-BUCKET"))

; http://www.surreysportspark.co.uk/media/PDFs/4759%20SSP%20Adult%20Spring%202017%20Swimming%20Timetable%20A4.pdf
(def swimming-spring-term-days
  (tt/days-seq (t/date-time 2017 1 9) (t/date-time 2017 4 2)))

(defn spring-term-events [d]
  "Return a list of events for the given day, or nil if no events"
  (cond
    (pr/saturday? d) [(tt/new-event d [ 9 00] [11 00] "50m Lane Swim" #{:50m :lane})
                      (tt/new-event d [11 30] [16 30] "25m Lane Swim" #{:25m :lane :shallow})
                      (tt/new-event d [11 00] [14 00] "Family Swim"   #{:family :shallow})
                      (tt/new-event d [16 30] [18 00] "25m Lane Swim" #{:25m :lane})]
    (pr/sunday? d)   [(tt/new-event d [11 00] [17 00] "50m Lane Swim" #{:50m :lane :shallow})
                      (tt/new-event d [11 00] [13 30] "Family Swim"   #{:family :shallow})]))

(def pred-50m-lane-swim? (tt/tagged-predicate #{:50m :lane}))

(->> swimming-spring-term-days
     (map #(t/from-time-zone % tz-london))
     (map spring-term-events)
     (tt/flatten-events)
     (filter pred-50m-lane-swim?)
     (map tt/create-event)
     (tt/append-events-to-cal (tt/new-cal))
     (tt/print-cal)
     (tt/publish bucket-name "surrey-sports-park/50m-lane-swimming.ics")
     (.getETag)
     (println))
