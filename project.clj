(defproject rmsbot "0.1.0-SNAPSHOT"
  :description "rmsbot"
  :url "http://github.com/gre/rmsbot"
  :license {:name "Affero General Public License"
            :url "https://gnu.org/licenses/agpl.html"}
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [twitter-api "0.7.8"]
    [org.clojure/core.async "0.1.346.0-17112a-alpha"]
    [twitter-streaming-client "0.3.2"]
    [overtone "0.9.1"]
    ]
  :main ^:skip-aot rmsbot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
