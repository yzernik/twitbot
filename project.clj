(defproject rmsbot "0.1.0-SNAPSHOT"
  :description "rmsbot"
  :url "http://github.com/gre/rmsbot"
  :license {:name "Affero General Public License"
            :url "https://gnu.org/licenses/agpl.html"}
  :dependencies [[org.clojure/clojure "1.6.0"] [twitter-api "0.7.8"]]
  :main ^:skip-aot rmsbot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
