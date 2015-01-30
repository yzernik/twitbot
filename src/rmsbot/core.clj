(ns rmsbot.core
  (:gen-class)
  (:require
    [rmsbot.detectlang :as detectlang]
    [rmsbot.twitter :as twitter]))

; Hi guys :-)

; Let's try some Clojure today =)

; The clojure documentation can be found on: http://clojure.org/
; or if you miss inspiration for HTTP usage: https://github.com/ornicar/vindinium-starter-clojure/blob/master/src/vindinium/core.clj

; Have fun !

(require 'clojure.edn)

(def messages (clojure.edn/read-string (slurp "messages.edn")))

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

(defn pick-answer
  "Pick an answer for a given topic and language. The topic comes from messages.edn."
  [topic lang]
  (rand-nth (get (:messages topic) (keyword lang) (:en (:messages topic))))
)

(defn tweet-topic
  "Find a matching topic for the input tweet message, nil if no match"
  [message]
  (first (filter (fn [m] (some #(.contains message %) (:keywords m))) messages))
)

(defn tweet-answer
  "Return the text to answer to a message, or nil if no match. Example: 'I love linux' => 'BTW it's GNU/Linux'"
  [message]
  (let [
    lang (detectlang/identify message)
    topic (tweet-topic message)]
    ((fnil #(pick-answer % lang) nil) topic))
)

(defn -main
  "RMS bot main function."
  [& args]

  ; TODO we need to iterate over tweets-channel
  ; See also http://clojuredocs.org/clojure.core.async/go-loop

  ; then for each tweet apply following:

  (let [
        [tweet-id tweet-text] ["42424242424242" "I love linux"] ; mock data
        to-tweet-text (tweet-answer tweet-text)]
    (if to-tweet-text
      (twitter/tweet to-tweet-text tweet-id)
      ())
    )
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
