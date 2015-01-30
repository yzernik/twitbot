(ns rmsbot.twitter
  (:use
    [rmsbot.config]
    [twitter.oauth]
    [twitter.callbacks]
    [twitter.callbacks.handlers]
    [twitter.api.streaming]
    [twitter.api.restful])
  (:require
    [clojure.data.json :as json]
    [http.async.client :as ac])
  (:import
    (twitter.callbacks.protocols SyncSingleCallback)))

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
