(ns rmsbot.core
  (:gen-class))

; Hi guys :-)

; Let's try some Clojure today =)

; The clojure documentation can be found on: http://clojure.org/
; or if you miss inspiration for HTTP usage: https://github.com/ornicar/vindinium-starter-clojure/blob/master/src/vindinium/core.clj

; Have fun !

(require 'clojure.edn)


(defn tweets-channel
  "Create a channel of tweets"
  []
  ; elements pushed in the channel are [ the-tweet-id the-tweet-content topic ] 
  ; example: ["424242424242" "I love linux" "linux"]
  ; example of topics: ("linux", "opensource", ...)

  ; TODO figure out how to return a "channel" from the twitter stream api

  ; TODO ideally, we should also try to use the descriptive sheet "_index" of the Spreadsheet to search & exclude tweets.
  ; this can come in a second priority feature
)

(defn detect-language
  "Determine the language of a tweet"
  [tweet-text]
  ; Use https://detectlanguage.com/ for this
  "en"
)

(defn pick-answer
  "Pick an answer for a given topic and language. For instance, 'linux' and 'fr' might be: 'Vous devez dire GNU/Linux.'"
  [topic lang]
  ; TODO a tuple of [topic lang] can have multiple variants. We can randomly pick one.
  ; TODO Ideally we should use the Google Spreadsheet, retrieve the sheet of the topic, the language column, and then randomly pick one row
  "This is GNU/Linux, not Linux!"
)

(defn tweet
  "Tweet an answer to a given tweet (referenced by id) – or just tweet something when tweet-parent-id is nil"
  [tweet-text tweet-parent-id]

  (println "tweeting..." tweet-text)
  ; TODO obviously use the twitter API
  true
)

(defn -main
  "RMS bot main function."
  [& args]

  ; TODO we need to iterate over tweets-channel
  ; See also http://clojuredocs.org/clojure.core.async/go-loop

  ; then for each tweet apply following:

  (let [
        [tweet-id tweet-text topic] ["42424242424242" "I love linux" "linux"] ; mock data
        lang (detect-language tweet-text)
        to-tweet-text (pick-answer topic lang)]
    (tweet to-tweet-text tweet-id))
  ; should this by try catched in the main loop? :-D
)


;
;  This file is part of rmsbot.
; 
;  Copyright 2015 Zengularity
; 
;  rmsbot is free software: you can redistribute it and/or modify
;  it under the terms of the AFFERO GNU General Public License as published by
;  the Free Software Foundation.
; 
;  rmsbot is distributed "AS-IS" AND WITHOUT ANY WARRANTY OF ANY KIND,
;  INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
;  NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR PURPOSE. See
;  the AFFERO GNU General Public License for the complete license terms.
; 
;  You should have received a copy of the AFFERO GNU General Public License
;  along with rmsbot.  If not, see <http://www.gnu.org/licenses/agpl-3.0.html>
;
