(ns twitbot.core
  (:gen-class)
  (:use [twitbot.config])
  (:require
    [twitbot.detectlang :as detectlang]
    [twitbot.twitter :as twitter]))

(require 'clojure.edn)

(defn pp [o] (let [_ (clojure.pprint/pprint o)] o))

(def messages (clojure.edn/read-string (slurp "messages.edn")))

(defn pick-answer
  "Pick an answer for a given topic and language. Ex: 'linux' 'fr'"
  [topic lang]
  (let [
    topic-data (get messages (keyword topic))]
    (rand-nth (get (:messages topic-data) (keyword lang) (:en (:messages topic-data))))))

(defn tweet [topic original-tweet]
    (let [
          message (:text original-tweet)
          tweet-id (:id original-tweet)
          lang (detectlang/identify message)
          answer (pick-answer topic lang)
          to-tweet-text (str "@" (-> original-tweet :user :screen_name) " " answer) ]
      (println "\n" (str "[" topic " " lang " " tweet-id "]") ":" message "\n ==>" to-tweet-text)
      (twitter/tweet to-tweet-text tweet-id)))

(defn -main
  "bot main function."
  [& args]

  (doseq [topic (keys messages)]
    (let [refresh-rate (:refresh-rate config)
          t (get messages topic) ]
      (twitter/stream
        (:keywords t)
        (:exclude t)
        #(tweet topic %)
        refresh-rate
        (int (rand refresh-rate))
        ))))


;
;  This file is part of twitbot.
; 
;  Copyright 2015 Zengularity
; 
;  twitbot is free software: you can redistribute it and/or modify
;  it under the terms of the AFFERO GNU General Public License as published by
;  the Free Software Foundation.
; 
;  twitbot is distributed "AS-IS" AND WITHOUT ANY WARRANTY OF ANY KIND,
;  INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
;  NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR PURPOSE. See
;  the AFFERO GNU General Public License for the complete license terms.
; 
;  You should have received a copy of the AFFERO GNU General Public License
;  along with twitbot.  If not, see <http://www.gnu.org/licenses/agpl-3.0.html>
;
