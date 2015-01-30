(ns rmsbot.core
  (:gen-class)
  (:require
    [clojure.core.async :as async :refer [chan go-loop <! put!]]
    [rmsbot.detectlang :as detectlang]
    [rmsbot.twitter :as twitter]))

(require 'clojure.edn)

(defn pp [o] (let [_ (clojure.pprint/pprint o)] o))

(def messages (clojure.edn/read-string (slurp "messages.edn")))

(defn pick-answer
  "Pick an answer for a given topic and language. Ex: 'linux' 'fr'"
  [topic lang]
  (let [
    topic-data (get messages (keyword topic))]
    (rand-nth (get (:messages topic-data) (keyword lang) (:en (:messages topic-data))))
    )
)

(defn -main
  "RMS bot main function."
  [& args]

  (defn tweet [topic original-tweet]
      (let [
            message (:text original-tweet)
            tweet-id (:id original-tweet)
            lang (detectlang/identify message)
            answer (pick-answer topic lang)
            to-tweet-text (str "@" (-> original-tweet :user :screen_name) " " answer) ]
        (println "\n" (str "[" topic " " lang " " tweet-id "]") ":" message "\n ==>" to-tweet-text)
        (twitter/tweet to-tweet-text tweet-id)))

  (doseq [topic (keys messages)]
    (let [ t (get messages (pp topic)) ]
      (twitter/stream
        (:keywords t)
        (:exclude t)
        #(tweet topic %))))
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
