(ns fodsearch-api.domain.category.interface
  (:require
   [malli.core :as m]
   [fodsearch-api.domain.category.impl :as impl]))

(def Category
  [:or
   [:map
    [:id uuid?]
    [:name string?]]
   nil?])

(defn get-all
  "Get all the categories."
  []
  (into [] (impl/select-all)))
(m/=> get-all
      [:=> :cat [:vector Category]])

(defn find-one
  "Get a single category by its value."
  [by value]
  (let [result (impl/select by value)]
    (if (seq result)
      (into {by value}
            (first result))
      nil)))
(m/=> find-one
      [:=> [:cat keyword? int?] Category])

(comment
  (get-all)
  (find-one :name "fruit")
  :comment)
