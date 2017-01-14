# timetable

A Clojure library designed to scrape websites for timetable data
 and publish them as [iCal](https://en.wikipedia.org/wiki/ICalendar).

## Usage

Run with the following env variables:

* `S3-BUCKET` - S3 Bucket in which to upload calendar
* `AWS-ACCESS-KEY`
* `AWS-SECRET-KEY`

# Sites

## Surrey Sports Park - 50m Lane Swimming

Timetable is published as a PDF - see [Spring timetable](http://www.surreysportspark.co.uk/media/PDFs/4759%20SSP%20Adult%20Spring%202017%20Swimming%20Timetable%20A4.pdf)

Published as [50m Lane Swimming](https://s3-eu-west-1.amazonaws.com/devstopfix-timetables/surrey-sports-park/50m-lane-swimming.ics)

Todo:

* Parse the [announcements page](http://www.surreysportspark.co.uk/guestinformation/changes/) for galas that close the pool and remove the event from the calendar

## License

Copyright © 2017 J Every

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
