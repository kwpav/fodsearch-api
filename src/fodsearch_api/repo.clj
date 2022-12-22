(ns fodsearch-api.repo
  (:require
   [fodsearch-api.db :as db]
   [malli.generator :as mg]))

;; (def Type
;;   [:enum :safe :moderate :unsafe])

;; (def Category
;;   [:enum
;;    "Vegetable"
;;    "Fruit"
;;    "Cereal, Grain, Nut, Seed, Flower"
;;    "Meat, Egg, Legume, Soy Protein"
;;    "Dairy Alternative"
;;    "Sweetener, Sauce, Condiment"
;;    "Sweet, Snack"
;;    "Drink"])

;; (def Ingredient
;;   [:map
;;    [:id int?]
;;    [:info string?]
;;    [:name string?]
;;    [:category Category]
;;    [:type Type]])

;; (defn gen-ingredient
;;   ([] (gen-ingredient {}))
;;   ([data]
;;    (merge (mg/generate Ingredient) data)))

#_(defn get-ingredient-by-id
  [id]
  (gen-ingredient {:id id}))

#_(defn get-ingredients
  []
  (mg/sample Ingredient {:size 10}))

(defn get-ingredients
  "Select all ingredients."
  []
  (let [query {:select [:*]
               :from   [:ingredient]}]
    (db/exec! query)))

(defn get-ingredient-by-id
  "Select a single ingredient by its id."
  [id]
  (let [query {:select [:*]
               :from   [:ingredient]
               :where  [:= :id id]}]
    (first (db/exec! query))))

(defn get-type-by-id
  "Select a single type by its id."
  [id]
  (let [query {:select [:*]
               :from   [:type]
               :where  [:= :type.id id]}]
    (first (db/exec! query))))

(defn get-types
  "Select all types."
  []
  (let [query {:select [:*]
               :from   [:type]}]
    (db/exec! query)))

(defn get-category-by-id
  "Select a single category by its id."
  [id]
  (let [query {:select [:*]
               :from   [:category]
               :where  [:= :category.id id]}]
    (first (db/exec! query))))

(defn get-categories
  "Select all categories."
  []
  (let [query {:select [:*]
               :from   [:category]}]
    (db/exec! query)))

(comment
  (map second (get-types))
  (get-categories)
  ,)
