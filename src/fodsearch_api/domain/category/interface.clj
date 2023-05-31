(ns fodsearch-api.domain.category.interface
  (:require
   [malli.core :as m]
   [fodsearch-api.domain.category.impl :as category]))

(def Category
  [:or
   [:map
    [:id uuid?]
    [:name string?]]
   nil?])

(defn get-all
  "Get all the categories."
  [app-config]
  (into [] (category/select-all app-config)))
(m/=> get-all
      [:=> :cat [:vector Category]])

(defn find-by-id
  "Get a single category by its value."
  [value app-config]
  (let [result (category/find-by-id value app-config)]
    (if (seq result)
      (first result)
      nil)))
(m/=> find-by-id
      [:=> [:cat keyword? int?] Category])

(comment
  (get-all {})
  :comment)
