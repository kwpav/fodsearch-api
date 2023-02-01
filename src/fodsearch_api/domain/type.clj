(ns fodsearch-api.domain.type
  (:require
   [fodsearch-api.repo.type :as repo]
   [malli.core :as m]))

(def Type
  [:or
   [:map
    [:id int?]
    [:name string?]]
   nil?])

(defn- type->json
  "Convert repo result into a map that can be converted to JSON."
  [{:type/keys [id name] :as _type}]
  {:id id :name name})

(defn get-all
  "Get all of the types."
  []
  (let [types (repo/select-all)]
    (mapv type->json types)))
(m/=> get-all
      [:=> :cat [:vector Type]])

(defn find-one
  "Get a single type by its value."
  [by value]
  (let [[type] (repo/select by value)]
    (if type
      (type->json type)
      nil)))
(m/=> find-one
      [:=> [:cat keyword? int?] Type])

(defn edit
  "Update a single category by its id."
  [{:keys [id] :as type}]
  (let [_ (repo/update-by-id type)]
    (find-one :id id)))

(defn create
  "Create a new type."
  [{name :name :as type}]
  (let [_ (repo/insert type)]
    (find-one :name name)))

(defn delete
  "Delete a single type by its value."
  [by value]
  (condp = by
    :id (do (repo/delete-by-id value) nil)))
