(ns twitbot.twitter
  (:use
    [twitbot.config]
    [overtone.at-at]
    [twitter.oauth]
    [twitter.callbacks]
    [twitter.callbacks.handlers]
    [twitter.api.search]
    [twitter.api.streaming]
    [twitter.api.restful])
  (:require
    [twitter-streaming-client.core :as client]
    [twitter.oauth :as oauth]
    [clojure.data.json :as json]
    [http.async.client :as ac])
  (:import
    (twitter.callbacks.protocols SyncSingleCallback)
    (twitter.callbacks.protocols AsyncStreamingCallback)))

(defn pp [o] (let [_ (clojure.pprint/pprint o)] o))

(def my-creds (make-oauth-creds (:twitter-key config)
                (:twitter-secret config)
                (:twitter-accesstoken config)
                (:twitter-accesstoken-secret config)))

(defn tweet
  [message parent-id]
  (statuses-update :oauth-creds my-creds
    :params {
      :status message,
      :in_reply_to_status_id parent-id
    }))

(defn follow [screen-name]
  (friendships-create :oauth-creds my-creds :params { :screen_name screen-name }))

(defn star [id]
  (favorites-create :oauth-creds my-creds :params { :id id }))

(defn retweet [id]
  (statuses-retweets-id :oauth-creds my-creds :params { :id id }))

(defn create-stream [term]
  (client/create-twitter-stream twitter.api.streaming/statuses-filter :oauth-creds my-creds :params {:track term}))

(defn start-stream [stream]
  (client/start-twitter-stream stream))

(defn filter-tweets [tweets keywords exclude]
  (filter (fn [t] (and
             (not (= (-> t :user :screen_name) (:twitter-screen-name config)))
             (every? #(not (.contains (.toLowerCase (:text t)) (.toLowerCase %))) exclude))) tweets))

(defn score-tweet [t]
  (- (-> t :user :followers_count) (/ (-> t :user :friends_count) 3)))

(defn pick-interesting-tweet [tweets]
  (get
    (vec (reverse (sort-by score-tweet tweets))) ; sort by best tweets
    (int (* (rand) (rand) (count tweets))) ; distribution that will favorize the best tweets
    ))

; TODO only use one stream for all: track all keywords of all topics at once and do more logic on our side.

(defn stream [topic callback]
  (let [
        topicid (:topic topic)
        keywords (:keywords topic)
        exclude (get topic :exclude [])
        refresh-rate (get topic :rate (get config :refresh-rate 60000))
        delay-ms (int (rand refresh-rate))
        stream (create-stream (clojure.string/join "," keywords))
        _ (start-stream stream)
        my-pool (mk-pool) ]

    (after
      delay-ms
      (fn[]
      (every
        refresh-rate
        (fn[]
          (let [q (client/retrieve-queues stream)
                all (:tweet q)
                tweets (filter-tweets all keywords exclude)
                _ (println (str "[" topicid "] from " (count all) " tweets " (count tweets) " matching (rate: " refresh-rate " ms)\n"))
                t (pick-interesting-tweet tweets)]
            (if t (callback t))))
        my-pool))
      my-pool)))


; (defn parse-date
;   [date]
;   (.parse (new java.text.SimpleDateFormat "EEE MMM dd HH:mm:ss Z yyyy" java.util.Locale/ENGLISH) date))
; 
; (defn pp [o] (let [_ (clojure.pprint/pprint o)] o))
; 
; (defn tweet-query [keywords exclude] (clojure.string/join " " (concat keywords (map #(str "-" %) exclude))))
; 
; (defn search-new-tweets
;   [keywords exclude after-time]
;   (let [response (search :oauth-creds my-creds :params {:q (tweet-query keywords exclude)})]
;     (if (not= (-> response :status :code) 200) []
;       (filter
;         #(> (.compareTo (-> % :created_at parse-date) after-time) 0)
;         (-> response :body :statuses))
;       )))
; 
;  (defn date-now [ deltaTime ] (new java.util.Date (+ (.getTime (new java.util.Date)) deltaTime)))
; 
; (defn stream-by-search-loop [interval keywords exclude callback]
;   (let [
;     from-time (date-now (- interval))
;     tweets (search-new-tweets keywords exclude from-time)]
;     (pp (str "nb tweets from search: " (count tweets)))
;   (doseq [t tweets] (callback t))
; 
; 
;   (Thread/sleep interval)
;   (recur interval keywords exclude callback)))
; 
; (defn stream-by-search [keywords exclude callback]
;   (stream-by-search-loop 60000 keywords exclude callback))
; 
; (stream-by-search ["linux"] ["gnu" "kernel"] pp)

