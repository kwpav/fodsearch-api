(ns fodsearch.category.interface
  (:require
   [malli.core :as m]
   [fodsearch.category.repo :as repo]))

(def Category
  [:or
   [:map
    [:id int?]
    [:name string?]]
   nil?])

(defn- category->json
  [{:category/keys [id name] :as _category}]
  {:id id :name name})

(defn get-all
  "Get all the categories."
  []
  (let [categories (repo/select-all)]
    (mapv category->json categories)))
(m/=> get-all
      [:=> :cat [:vector Category]])

(defn find-one
  "Get a single category by its value."
  [by value]
  (let [[category] (repo/select by value)]
    (if category
      (category->json category)
      nil)))
(m/=> find-one
      [:=> [:cat keyword? int?] Category])

(defn edit
  "Update a single category by its id."
  [{:keys [id] :as category}]
  (let [_ (repo/update-by-id category)]
    (find-one :id id)))

(defn create
  "Create a new category."
  [{name :name :as category}]
  (let [_ (repo/insert category)]
    (find-one :name name)))

(defn delete
  "Delete a single category by its value."
  [by value]
  (condp = by
    :id (do (repo/delete-by-id value) nil)))
