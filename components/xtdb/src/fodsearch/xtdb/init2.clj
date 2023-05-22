(ns fodsearch.xtdb.init2
  "Using xtdb 2.x"
  (:require
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [malli.core :as m]
   [malli.provider :as mp]
   [malli.transform :as mt]
   [meander.match.epsilon :as mme]
   [xtdb.api :as xt]
   [xtdb.node :as xt.node]))

(defn- read-ingredients-csv
  "All ingredient data is in `resources/database/ingredients.csv`.
  This reads the csv and puts it into a map so it can be
  easily inserted into the DB with HoneySQL."
  []
  (letfn [(csv-data->maps [csv-data]
            (mapv zipmap
                  (->> (first csv-data) ;; First row is the header
                       (map keyword)
                       repeat)
                  (rest csv-data)))]
    (with-open [reader (io/reader (io/resource "xtdb/ingredients.csv"))]
      (csv-data->maps (csv/read-csv reader)))))

;; bunch of code taken from:
;; https://www.metosin.fi/blog/transforming-data-with-malli-and-meander/

(defn coercer [schema transformer]
  (let [valid?  (m/validator schema)
        decode  (m/decoder schema transformer)
        explain (m/explainer schema)]
    (fn [x]
      (let [value (decode x)]
        (when-not (valid? value)
          (m/-fail! ::invalid-input {:value   value
                                     :schema  schema
                                     :explain (explain value)}))
        value))))

(defn matcher [{:keys [pattern expression]}]
  (eval `(fn [data#]
           (let [~'data data#]
             ~(mme/compile-match-args
               (list 'data pattern expression)
               nil)))))

(def CSVIngredient (mp/provide (read-ingredients-csv)))

(def Ingredient
  [:map
   [:xt/id uuid?]
   [:name string?]
   [:info {:optional true} string?]
   [:category [:map
               [:xt/id uuid?]
               [:name string?]]]
   [:level [:map
            [:xt/id uuid?]
            [:name [:enum "safe" "moderate"]]]]])

(def transform
  (matcher
   {:pattern    '{:id       ?id
                  :name     ?name
                  :info     ?info
                  :category ?category
                  :level    ?level}
    :expression '{:xt/id    (random-uuid)
                  :name     ?name
                  :info     ?info
                  :category ?category
                  :level    ?level}}))

(def validate-input (coercer CSVIngredient (mt/default-value-transformer)))
(def validate-output
  (coercer
   Ingredient
   (mt/transformer
    (mt/string-transformer)
    (mt/default-value-transformer))))

(defn csv->levels
  [csv]
  (->> csv
       (mapv :level)
       (into #{})
       (mapv (fn [l]
               {:xt/id (random-uuid)
                :name  l}))))

(defn csv->categories
  [csv]
  (->> csv
       (mapv :category)
       (into #{})
       (mapv (fn [c]
               {:xt/id (random-uuid)
                :name  c}))))

(defn csv->ingredients
  [csv]
  (let [levels      (csv->levels csv)
        categories  (csv->categories csv)
        ingredients (->> csv
                         (map validate-input)
                         (mapv transform)
                         (mapv (fn [i]
                                 (-> i
                                     (assoc :level
                                            (first (filter #(= (:name %) (:level i)) levels)))
                                     (assoc :category
                                            (first (filter #(= (:name %) (:category i)) categories))))))
                         (mapv validate-output))]
    {:levels      levels
     :categories  categories
     :ingredients ingredients}))

(defonce my-node (atom (xt.node/start-node {:xtdb/server {:port 3001}
                                            :xtdb/pgwire {:port 5432}})))

(defn put-all
  [db data]
  (let [data* (into [] data)]
    (for [[k v] data*]
      (cond
        (map? v) (xt/submit-tx db [[:put k v]])
        (vector? v) (for [datum v]
                      (xt/submit-tx db [[:put k datum]]))))))

(comment
  (put-all @my-node (csv->ingredients (read-ingredients-csv)))

  ;xtdb2
  (xt/q @my-node '{:find [xt/id name]
                   :where [($ :levels [xt/id name])]})

  (xt/q @my-node '{:find [xt/id name]
                   :where [($ :categories [xt/id name])]})

  (xt/q @my-node '{:find  [name info category level]
                   :where [($ :ingredients [name info category level])]})

  (xt/q @my-node '{:find [name xt/id category level]
                   :where [($ :ingredients [xt/id name category level
                                            {:name "Splenda"}])]})

  (xt/q @my-node '{:find  [c]
                   :where [($ :ingredients [category])
                           [category c]]})

  (xt/q @my-node '{:find [name]
                   :where [[_ :ingredients name]]})
  
  (xt/status @my-node)

  (xt/submit-tx @my-node [[:evict :ingredients]])


  (def manifest
  {:xt/id :manifest
   :pilot-name "Johanna"
   :id/rocket "SB002-sol"
   :id/employee "22910x2"
   :badges "SETUP"
   :cargo ["stereo" "gold fish" "slippers" "secret note"]})

  (xt/submit-tx @my-node [[:put manifest]])

  :comment)
