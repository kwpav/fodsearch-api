(ns fodsearch-api.domain.ingredient
  (:require
   [malli.core :as m]
   [fodsearch-api.repo.ingredient :as repo]
   [fodsearch-api.domain.level :as level]
   [fodsearch-api.domain.category :as category]))

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
  []
  (repo/select-all))
(m/=> get-all
      [:=> :cat [:vector Ingredient]])

;; TODO implement this!
(defn search
  "Search for an ingredient that matches the given query string."
  [query]
  [])

(defn find-one
  "Get a single ingredient by its value."
  [by value]
  (let [result (repo/select by value)]
    (if (seq result)
      result
      nil)))
(m/=> find-one
      [:=> [:cat keyword? int?] Ingredient])

(comment
  (get-all)
  (find-one :id #uuid "290bc5fa-4afd-4f5c-85d1-987dc9b103b5")
  :comment)