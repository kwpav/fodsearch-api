(ns fodsearch-api.repo
  (:require [malli.core :as m]
            [malli.generator :as mg]))

(def Type
  [:enum :safe :moderate :unsafe])

(def Category
  [:enum
   "Vegetable"
   "Fruit"
   "Cereal, Grain, Nut, Seed, Flower"
   "Meat, Egg, Legume, Soy Protein"
   "Dairy Alternative"
   "Sweetener, Sauce, Condiment"
   "Sweet, Snack"
   "Drink"])

(def Ingredient
  [:map
   [:id int?]
   [:name string?]
   [:category Category]
   [:type Type]])

(defn gen-ingredient
  ([] (gen-ingredient {}))
  ([data]
   (merge (mg/generate Ingredient) data)))

(defn get-ingredient-by-id
  [id]
  (gen-ingredient {:id id}))

(defn get-ingredients
  []
  (mg/sample Ingredient {:size 10}))

(comment
  (merge (mg/generate Ingredient)
         {:name "test"})

  (gen-ingredient {:name  "Artichoke"})
  (gen-ingredient)

  (get-ingredients)

  ,)
