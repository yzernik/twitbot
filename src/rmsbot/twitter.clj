(ns rmsbot.twitter
  (:use
    [twitter.oauth]
    [twitter.callbacks]
    [twitter.callbacks.handlers]
    [twitter.api.restful])
  (:import
    (twitter.callbacks.protocols SyncSingleCallback)))

(def config (clojure.edn/read-string (slurp "config.edn")))

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
