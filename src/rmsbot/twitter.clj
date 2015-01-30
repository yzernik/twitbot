(ns rmsbot.twitter
  (:use
    [rmsbot.config]
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

(def my-creds (make-oauth-creds (:twitter-key config)
                (:twitter-secret config)
                (:twitter-accesstoken config)
                (:twitter-accesstoken-secret config)
                ))

(defn tweet
  [message parent-id]
  (statuses-update :oauth-creds my-creds
    :params {
      :status message,
      :in_reply_to_status_id parent-id
    })
  )

(defn parse-date
  [date]
  (.parse (new java.text.SimpleDateFormat "EEE MMM dd HH:mm:ss Z yyyy" java.util.Locale/ENGLISH) date))

(defn pp [o] (let [_ (clojure.pprint/pprint o)] o))

(defn tweet-query [keywords exclude] (clojure.string/join " " (concat keywords (map #(str "-" %) exclude))))

(defn search-new-tweets
  [keywords exclude after-time]
  (let [response (search :oauth-creds my-creds :params {:q (tweet-query keywords exclude)})]
    (if (not= (-> response :status :code) 200) []
      (filter
        #(> (.compareTo (-> % :created_at parse-date) after-time) 0)
        (-> response :body :statuses))
      )))

 (defn date-now [ deltaTime ] (new java.util.Date (+ (.getTime (new java.util.Date)) deltaTime)))

(defn stream-by-search-loop [interval keywords exclude callback]
  (let [
    from-time (date-now (- interval))
    tweets (search-new-tweets keywords exclude from-time)]
    (pp (str "nb tweets from search: " (count tweets)))
  (doseq [t tweets] (callback t))


  (Thread/sleep interval)
  (recur interval keywords exclude callback)))

(defn stream-by-search [keywords exclude callback]
  (stream-by-search-loop 60000 keywords exclude callback))

; (stream-by-search ["linux"] ["gnu" "kernel"] pp)

(defn create-stream [term]
  (client/create-twitter-stream twitter.api.streaming/statuses-filter :oauth-creds my-creds :params {:track term}))

(defn start-stream [stream]
  (client/start-twitter-stream stream))

(def my-pool (mk-pool))

(defn stream [keywords exclude callback]
  (let [stream (create-stream (clojure.string/join " OR " keywords))
        _ (start-stream stream)]

    (every 1000 (fn[](let [q (client/retrieve-queues stream)
                           tweets (:tweet q)]
                       (doseq [t tweets]
                         (if (and
                               (not (= (-> t :user :screen_name) "rmsthebot"))
                               (every? #(not (.contains (.toLowerCase (:text t)) (.toLowerCase %))) exclude))
                           (callback t))
                         ))) my-pool)))
