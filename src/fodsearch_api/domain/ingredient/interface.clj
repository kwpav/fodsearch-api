(ns fodsearch-api.domain.ingredient.interface
  (:require
   [malli.core :as m]
   [fodsearch-api.domain.ingredient.impl :as ingredient]
   [fodsearch-api.domain.level.interface :as level]
   [fodsearch-api.domain.category.interface :as category]))

(def Ingredient
  [:or
   [:map
    [:id uuid?]
    [:name string?]
    [:info {:optional true} string?]
    [:category category/Category]
    [:level level/Level]]
   nil?])

(defn get-all
  "Get all ingredients."
  [app-config]
  (ingredient/get-all app-config))
(m/=> get-all
      [:=> :cat [:vector Ingredient]])

;; TODO implement this!
(defn search
  "Search for an ingredient that matches the given query string."
  [query app-config]
  [])

(defn find-by-id
  "Get a single ingredient by its id."
  [value app-config]
  (let [result (ingredient/find-by-id value app-config)]
    (if (seq result)
      result
      nil)))
(m/=> find-by-id
      [:=> [:cat keyword? int?] Ingredient])

(comment
  (get-all {})
  (find-by-id #uuid "290bc5fa-4afd-4f5c-85d1-987dc9b103b5" {})
  :comment)
