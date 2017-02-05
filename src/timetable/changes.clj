(ns timetable.changes
  (:require [hickory.core :as h]
            [clj-http.client :as client]))

(defn changes [^String html parser]
  "Apply parser function to HTML"
  (-> html
      (h/parse)
      (h/as-hickory)
      (parser)))

(defn changes-file [f parser]
  (changes (slurp f) parser))

(defn changes-url [url parser]
  (-> url
      (client/get {:accept           "text/html"
                   :throw-exceptions true})
      (:body)
      (changes parser)))
