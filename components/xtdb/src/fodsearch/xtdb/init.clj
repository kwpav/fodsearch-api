(ns fodsearch.xtdb.init
  (:require
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [malli.core :as m]
   [malli.provider :as mp]
   [malli.transform :as mt]
   [meander.match.epsilon :as mme]
   [xtdb.api :as xt]))

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
               {:xt/id      (random-uuid)
                :level/name l}))))

(defn csv->categories
  [csv]
  (->> csv
       (mapv :category)
       (into #{})
       (into [])
       (mapv (fn [c]
               {:xt/id      (random-uuid)
                :category/name c}))))

(defn csv->ingredients
  [csv]
  (let [levels      (csv->levels csv)
        categories  (csv->categories csv)
        ingredients (->> csv
                         (map validate-ingredient-input)
                         (mapv transform-ingredient)
                         (mapv (fn [i]
                                 (-> i
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

(defonce node (xt/start-node {}))

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

  (put-all node ingredients)

  (xt/q (xt/db node)
        '{:find  [?id ?name]
          :keys  [id name]
          :where [[?id :level/name ?name]]})

  (xt/q (xt/db node)
        '{:find  [?id ?name]
          :keys  [id name]
          :where [[?id :category/name ?name]]})

  (xt/q (xt/db node)
        '{:find  [?name]
          :where [[?id :level/name "safe"]
                  [e :id/level ?id]
                  [e :ingredient/name ?name]]})

  (xt/q (xt/db node)
        '{:find  [?name]
          :in    [level-name]
          :where [[?id :level/name level-name]
                  [e :id/level ?id]
                  [e :ingredient/name ?name]]}
        "safe"
        #_"moderate")

    ;; get all ingredients - flat
  (xt/q (xt/db node)
        '{:find  [?id ?name ?info ?level ?category]
          :where [[?id :ingredient/name ?name]
                  [?id :ingredient/info ?info]
                  [?id :id/level ?level-id]
                  [?id :id/category ?category-id]
                  [?level-id :level/name ?level]
                  [?category-id :category/name ?category]]})

  (mapv first
        (xt/q
         (xt/db node)
         '{:find  [(pull ?ingredient [(:xt/id {:as :id})
                                      (:ingredient/name {:as :name})
                                      (:ingredient/info {:as :info})
                                      {(:id/level {:as :level})
                                       [(:xt/id {:as :id})
                                        (:level/name {:as :name})]}
                                      {(:id/category {:as :category})
                                       [(:xt/id {:as :id})
                                        (:category/name {:as :name})]}])]
           :where [[?ingredient :ingredient/name ?name]]}))

  ;; ?????
  (xt/q (xt/db node)
        '{:find  [?id ?name ?info ?category ?level]
          :keys  [id name info category level]
          :where [[?level-id :level/name "moderate"]
                  [e :id/level ?level-id]
                  [e :id/category ?category-id]
                  [?id :ingredient/name ?name]
                  [e :ingredient/info ?info]
                  [?category-id :category/name ?category]
                  [?level-id :level/name ?level]]})

  (xt/q (xt/db node)
        '{:find  [?name]
          :where [[id :level/name "safe"]
                  [e :id/level]
                  [e :ingredient/name ?name]]})

  (def manifest
    {:xt/id       :manifest
     :pilot-name  "Johanna"
     :id/rocket   "SB002-sol"
     :id/employee "22910x2"
     :badges      "SETUP"
     :cargo       ["stereo" "gold fish" "slippers" "secret note"]})

  (def manifest
    {:xt/id       :manifest
     :pilot-name  "Johanna"
     :id/rocket   "SB002-sol"
     :id/employee "22910x2"
     :badges      "SETUP"
     :cargo       ["stereo" "gold fish" "slippers" "secret note"]})

  (xt/submit-tx node [[::xt/put manifest]])

  (xt/sync node)

  (xt/entity (xt/db node) :manifest)

  :comment)

#_(comment
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
