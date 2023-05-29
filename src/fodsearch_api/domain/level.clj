(ns fodsearch-api.domain.level
  (:require
   [malli.core :as m]
   [fodsearch-api.repo.level :as repo]))

(def Level
  [:or
   [:map
    [:id uuid?]
    [:name string?]]
   nil?])

(defn get-all
  "Get all of the levels."
  []
  (into [] (repo/select-all)))
(m/=> get-all
      [:=> :cat [:vector Level]])

(defn find-one
  "Get a single level by its value."
  [by value]
  (let  [result (repo/select by value)]
    (if (seq result)
      (into {by value}
            (first result))
      nil)))
(m/=> find-one
      [:=> [:cat keyword? int?] Level])


(comment
  (get-all)
  (find-one :name "safe")
  :comment)
