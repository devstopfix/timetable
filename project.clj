(defproject timetable "1.17.106"
  :description "Generate iCal from timetables"
  :url "https://github.com/devstopfix/timetable"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:uberjar {:aot :all}}
  :uberjar-name "surreysportspark-1.0.0.jar"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-icalendar "0.1.4"]
                 [clj-time "0.13.0"]
                 [pandect "0.6.1"]
                 [clj-aws-s3 "0.3.10" :exclusions [joda-time]]
                 [hickory "0.7.0"]
                 [clj-http "2.3.0"]
                 [uswitch/lambada "0.1.2"]])
