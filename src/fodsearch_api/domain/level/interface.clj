(ns fodsearch-api.domain.level.interface
  (:require
   [malli.core :as m]
   [fodsearch-api.domain.level.impl :as level]))

(def Level
  [:or
   [:map
    [:id uuid?]
    [:name string?]]
   nil?])

(defn get-all
  "Get all of the levels."
  [app-config]
  (into [] (level/get-all app-config)))
(m/=> get-all
      [:=> :cat [:vector Level]])

(defn find-by-id
  "Get a single level by its value."
  [value app-config]
  (let  [result (level/find-by-id value app-config)]
    (if (seq result)
      (first result)
      nil)))
(m/=> find-by-id
      [:=> [:cat keyword? int?] Level])

(comment
  (get-all {})
  :comment)
