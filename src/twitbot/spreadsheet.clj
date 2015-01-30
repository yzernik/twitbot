(ns twitbot.spreadsheet
  (:use twitbot.config)
  (:require
    [clojure.string :as str]
    [clojure.xml :as xml]
    [clojure.data.json :as json]
    [clj-http.client :as http]))

(require 'clojure.edn)

(defn pp [o] (let [_ (clojure.pprint/pprint o)] o))

(defn tuples-to-key-value-vector [tuples]
  (->> tuples
       (group-by first)
       (map #(let [[k v] %] [k (vec (map second v))]))
       (reduce conj {})))

(defn get-index-sheet [id]
  (:body (http/get (str "https://spreadsheets.google.com/feeds/list/" id "/od6/public/values?alt=json") {:as :json})))

(defn get-sheet-cells [id sheet-index]
  (:body (http/get (str "https://spreadsheets.google.com/feeds/list/" id "/" sheet-index "/public/values?alt=json") {:as :json})))

(defn parse-index-sheet [json]
  (->> json
       :feed
       :entry
       (map (fn [entry]
              {:topic (-> entry :gsx$topic :$t)
               :keywords (str/split (-> entry :gsx$keywords :$t) #"[|]")
               :exclude (str/split  (-> entry :gsx$exclude :$t)  #"[|]")}))))

(defn parse-sheet-cells [json]
  {:topic (-> json :feed :title :$t)
   :messages
   (tuples-to-key-value-vector (apply concat (->> json
         :feed
         :entry
         (map (fn [entry]
             (filter
               #(not (nil? %))
               (map #(let [[k v] %
                           content (:$t v)]
                       (if (and (not (str/blank? content)) (.startsWith (name k) "gsx$"))
                         [ (keyword (.substring (name k) 4)) content ])) entry)))))))

   })

(defn gdocs-load-messages [id]
  (let [ topics (parse-index-sheet (get-index-sheet id))
        sheets (map #(parse-sheet-cells (get-sheet-cells id %)) (range 2 (+ 2 (count topics)))) ]

    (merge-with
      (fn [a b] (merge (first a) (first b)))
      (group-by #(keyword (:topic %)) topics)
      (group-by #(keyword (:topic %)) sheets))))
