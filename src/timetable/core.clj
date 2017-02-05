(ns timetable.core
  (:require [clj-icalendar.core :as ical]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [clj-time.periodic :as p]
            [clj-time.predicates :as pr]
            [clj-time.coerce :as c]
            [pandect.algo.sha1 :refer [sha1]]
            [aws.sdk.s3 :as s3]))

; Events are a period of time with tags
(defrecord Event [start-time end-time title tags])

(defn new-event [d start end title tags]
  "Create a new event on day between given start and end times.
   start and end are vectors of [hour min]."
  (let [[start-hour start-min] start
        [end-hour   end-min]   end]
    (->Event
      (-> d (.withHourOfDay start-hour) (.withMinuteOfHour start-min))
      (-> d (.withHourOfDay end-hour)   (.withMinuteOfHour end-min))
      title tags)))

(defn overlap-intervals? [event intervals]
  "Returns nil if the given event does not overlap any of the intervals,
   otherwise an interval"
  (let [interval (t/interval (.start-time event) (.end-time event))]
    (some #(t/overlaps? % interval) intervals)))

(def digest-list-of-strings (comp sha1 clojure.string/upper-case clojure.string/join))

(defn event-id [event salt]
  "ID of event is hash of it's title and period"
  (-> [salt]
      (conj (.start-time event))
      (conj (.end-time event))
      (conj (:title event))
      (digest-list-of-strings)))

(def flatten-events
  "Remove nil events and flatten"
  (comp flatten (partial remove nil?)))

(defn tagged-predicate [required-tags]
  "Make a predicate that tests if an Event has the given tags.
   Returns a function that can be used with filter and remove"
  (comp (partial clojure.set/subset? required-tags) :tags))


(defn new-cal []
  (ical/create-cal
    "Surrey Sports Park"
    "50M Lane Swimming"
    "V0.1" "EN"
    :name "50m Lane Swimming at Surrey Sports Park"
    :ttl "PT24H"))

(defn days-seq [start-date end-date]
  "Return a seq of days from start to end inclusive"
  (let [one-day (t/days 1)
        next-end-date (t/plus end-date one-day)]
    (take-while #(t/before? % next-end-date)
     (clj-time.periodic/periodic-seq start-date (t/days 1)))))

(def organizer "http://www.surreysportspark.co.uk/sports/swimming")

(defn create-event [event]
  (do
    (ical/create-event
     (c/to-date (.start-time event))
     (c/to-date (.end-time event))
     (:title event)
     :organizer organizer
     :location "Surrey Sports Park, GU2 7AD"
     :unique-id (event-id event organizer))))

(defn print-cal [cal]
  (ical/output-calendar cal))

(defn append-events-to-cal [cal events]
  (reduce (fn [cal event] (ical/add-event! cal event)) cal events))

(def ICAL-MIME-TYPE "text/calendar")

(defn publish [bucket key ^String cal]
  "Publish the calendar as key in bucket"
  {:pre [(string? bucket)
         (re-matches #"[a-z][a-z0-9\-]{0,61}[a-z0-9]" bucket)
         (string? key)
         (re-matches #"[0-9a-zA-z\/\-\.]+" key)
         (.startsWith cal "BEGIN:VCALENDAR")
         (.endsWith   cal "END:VCALENDAR")]}
  (let [access-key (System/getenv "AWS_ACCESS_KEY")
        secret-key (System/getenv "AWS_SECRET_KEY")
        cred {:access-key access-key
              :secret-key secret-key}]
    (s3/put-object cred bucket key cal
                   {:content-type ICAL-MIME-TYPE
                    :cache-control (format "max-age=%d" 360) })))

(defn publish-with-timestamp [bucket key ^String cal]
  "Publish the calendar to the bucket, and append current timestamp to key"
  (let [custom-formatter (f/formatter "yyyy.MM.dd.HH")
        suffix (f/unparse custom-formatter (t/now))
        key    (str key "."  suffix)]
    (publish bucket key cal)))
