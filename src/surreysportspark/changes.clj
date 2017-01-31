(ns surreysportspark.changes
  (:require [clj-time.core :as t]
            [hickory.zip :as hz]
            [clojure.zip :as zip]
            [clj-time.format :as f]))

(def datetime-format (f/formatter "YYYY MMMM d HH.mm ZZZ"))

(def line-matcher (partial re-find #"\w+ (\d+) (\w+), ([\d\.]+)[\s\D]+([\d\.]+)"))

(def this-year 2017)

(defn datetime [mmmm dd t]
    (->> [this-year mmmm dd t "Europe/London"]
        (clojure.string/join " ")
        (f/parse datetime-format)))

(defn parse-interval [line]
  (let [[_ dd mmmm st ed] line]
    (t/interval
      (datetime mmmm dd st)
      (datetime mmmm dd ed))))

(defn is-pool-closure-heading? [node]
  (and
    (= :h2 (:tag node))
    (= "Pool Closures" (first (:content node)))))

(defn find-pool-closure-heading [html]
  (loop [loc html]
    (when-not (zip/end? loc)
      (if (is-pool-closure-heading? (zip/node loc))
        loc
        (recur (zip/next loc))))))

(defn entries [loc]
  (loop [loc (zip/right loc)
         paragraphs []]
    (clojure.pprint/pprint (zip/node loc))
    (if (= :p (:tag (zip/node loc)))
      (recur
        (-> loc (zip/right))
        (conj paragraphs (:content (zip/node loc))))
      (flatten paragraphs))))

(defn parse-pool-closures [html]
  "Return a vector of intervals of pool closures.
   Input is HTML parsed by Hickory."
  (->> html
       (hz/hickory-zip)
       (find-pool-closure-heading)
       (entries)
       (map line-matcher)
       (remove nil?)
       (map parse-interval)))
