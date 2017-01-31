(ns timetable.changes
  (:require [hickory.core :as h]))


(defn changes [html parser]
  (-> html
      (h/parse)
      (h/as-hickory)
      (parser)))


(defn changes-file [f parser]
  (changes (slurp f) parser))

