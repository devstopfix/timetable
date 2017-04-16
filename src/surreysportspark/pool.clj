(ns surreysportspark.pool
  (:require [clj-time.core :as t]
            [clj-time.predicates :as pr]
            [surreysportspark.changes :as sspc]
            [timetable.core :as tt]
            [timetable.changes :as tc]))

; All our Events are in London (GMT or BST)
(def tz-london (t/time-zone-for-id "Europe/London"))

; We publish calenders to S3 bucket
(def bucket-name (System/getenv "S3_BUCKET"))

; http://www.surreysportspark.co.uk/media/PDFs/4759%20SSP%20Adult%20Spring%202017%20Swimming%20Timetable%20A4.pdf
(def swimming-summer-term-days
  (tt/days-seq (t/date-time 2017 4 3) (t/date-time 2017 7 9)))

(defn spring-term-events [d]
  "Return a list of events for the given day, or nil if no events"
  (cond
    (pr/saturday? d) [(tt/new-event d [ 9 00] [11 00] "50m Lane Swim" #{:50m :lane})
                      (tt/new-event d [11 30] [14 00] "Family Swim"   #{:family :shallow})
                      (tt/new-event d [11 30] [16 30] "25m Lane Swim" #{:25m :lane :shallow})]
    (pr/sunday? d)   [(tt/new-event d [11 00] [17 00] "50m Lane Swim" #{:50m :lane :shallow})
                      (tt/new-event d [11 00] [13 30] "Family Swim"   #{:family :shallow})]))

(def pred-50m-lane-swim? (tt/tagged-predicate #{:50m :lane}))

(defn gen-events [days]
  (->> days
       (map #(t/from-time-zone % tz-london))
       (map spring-term-events)
       (tt/flatten-events)))

(def changes-url "http://www.surreysportspark.co.uk/guestinformation/changes/")

(defn make-calendar []
  (let [closures (tc/changes-url changes-url sspc/parse-pool-closures)]
    (->> (gen-events swimming-summer-term-days)
         (filter pred-50m-lane-swim?)
         (remove #(tt/overlap-intervals? % closures))
         (map tt/create-event)
         (tt/append-events-to-cal (tt/new-cal))
         (tt/print-cal)
         (tt/publish-with-timestamp bucket-name "surrey-sports-park/50m-lane-swimming.ics")
         (.getETag)
         (println))))
