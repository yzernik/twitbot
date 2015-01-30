(ns rmsbot.twitter
  (:use
    [rmsbot.config]
    [twitter.oauth]
    [twitter.callbacks]
    [twitter.callbacks.handlers]
    [twitter.api.streaming]
    [overtone.at-at]
    [twitter.api.restful])
  (:require
    [twitter-streaming-client.core :as client]
    [twitter.oauth :as oauth]
    [clojure.data.json :as json]
    [http.async.client :as ac])
  (:import
    (twitter.callbacks.protocols SyncSingleCallback)
    (twitter.callbacks.protocols AsyncStreamingCallback)))

(require 'clojure.edn)

(def my-creds (make-oauth-creds (:twitter-key config)
                (:twitter-secret config)
                (:twitter-accesstoken config)
                (:twitter-accesstoken-secret config)
                ))

; simply retrieves the user, authenticating with the above credentials
(defn test-twitter
  [username]
  (users-show :oauth-creds my-creds :params {:screen-name username})
  )

(defn create-stream [term]
  (client/create-twitter-stream twitter.api.streaming/statuses-filter :oauth-creds my-creds :params {:track term}))

(defn start-stream [stream]
  (client/start-twitter-stream stream))

(def my-pool (mk-pool))

(defn stream [term callback]
  (let [stream (create-stream term)
        _ (start-stream stream)]

    (every 1000 (fn[](let [q (client/retrieve-queues stream)
                           tweets (:tweet q)]
                       (doseq [t tweets] (callback t)))) my-pool)))
