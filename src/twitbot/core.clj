(ns twitbot.core
  (:gen-class)
  (:use [twitbot.config])
  (:require
    [twitbot.spreadsheet :as spreadsheet]
    [twitbot.detectlang :as detectlang]
    [twitbot.twitter :as twitter]))

(require 'clojure.edn)

(defn pp [o] (let [_ (clojure.pprint/pprint o)] o))

(pp "Loading messages...")

(def messages
  (if-not (nil? (:messages-gdocs config))
    (spreadsheet/gdocs-load-messages (:messages-gdocs config))
    (clojure.edn/read-string (slurp "messages.edn"))))

(pp messages)

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
          screen-name (-> original-tweet :user :screen_name)
          to-tweet-text (str "@" screen-name " " answer) ]
      (println "\n" (str "[" topic " " lang " " tweet-id "]") ":" message "\n ==>" to-tweet-text)
      (if (get config :follow-on-tweet false)
        (twitter/follow screen-name))
      (if-not (get config :disable-tweet false)
        (twitter/tweet to-tweet-text tweet-id))))

(defn -main
  "bot main function."
  [& args]

  (doseq [topicname (keys messages)]
    (let [topic (get messages topicname)]
      (twitter/stream
        topic
        (case (:action topic)
          "reply" #(tweet topicname %)
          identity
        )))))


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
