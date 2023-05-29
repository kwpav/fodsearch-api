(ns fodsearch-api.database.init
  (:require
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [malli.core :as m]
   [malli.provider :as mp]
   [malli.transform :as mt]
   [meander.match.epsilon :as mme]
   [xtdb.api :as xt]
   [fodsearch-api.database.interface :as db]))

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
    (with-open [reader (io/reader (io/resource "ingredients.csv"))]
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
   [:ingredient/name string?]
   [:ingredient/info {:optional true} string?]
   [:id/category uuid?]
   [:id/level uuid?]])

(def Category
  [:map
   [:xt/id uuid?]
   [:category/name string?]])

(def Level
  [:map
   [:xt/id uuid?]
   [:category/name string?]])

(def transform-ingredient
  (matcher
   {:pattern    '{:id       ?id
                  :name     ?name
                  :info     ?info
                  :category ?category
                  :level    ?level}
    :expression '{:xt/id               (random-uuid)
                  :ingredient/name     ?name
                  :ingredient/info     ?info
                  :ingredient/category ?category
                  :ingredient/level    ?level}}))
(def validate-ingredient-input (coercer CSVIngredient (mt/default-value-transformer)))

(def validate-ingredient-output
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
               (let [id (random-uuid)]
                 {:xt/id      id
                  :level/id   id
                  :level/name l})))))

(defn csv->categories
  [csv]
  (->> csv
       (mapv :category)
       (into #{})
       (into [])
       (mapv (fn [c]
               (let [id (random-uuid)]
                 {:xt/id         id
                  :category/id   id
                  :category/name c})))))

(defn csv->ingredients
  [csv]
  (let [levels      (csv->levels csv)
        categories  (csv->categories csv)
        ingredients (->> csv
                         (map validate-ingredient-input)
                         (mapv transform-ingredient)
                         (mapv (fn [i]
                                 (-> i
                                     (assoc :ingredient/id  (:xt/id i))
                                     (assoc :id/level
                                            (->> levels
                                                 (filter #(= (:level/name %) (:ingredient/level i)))
                                                 first
                                                 :xt/id))
                                     (assoc :id/category
                                            (->> categories
                                                 (filter #(= (:category/name %) (:ingredient/category i)))
                                                 first
                                                 :xt/id))
                                     (dissoc :ingredient/level)
                                     (dissoc :ingredient/category)
                                     (cond-> (str/blank? (:ingredient/info i)) (dissoc :ingredient/info)))))
                         (mapv validate-ingredient-output))]
    {:levels      levels
     :categories  categories
     :ingredients ingredients}))

(defn put-all
  [db data]
  (let [data* (into [] data)]
    (for [[k v] data*]
      (cond
        (map? v) (xt/submit-tx db [[::xt/put v]])
        (vector? v) (for [datum v]
                      (xt/submit-tx db [[::xt/put datum]]))))))

(comment

  (def ingredients
    (csv->ingredients (read-ingredients-csv)))

  (put-all db/node ingredients)
  (xt/sync db/node)

  :comment)
