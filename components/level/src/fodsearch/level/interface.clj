(ns fodsearch.level.interface
  (:require
   [malli.core :as m]
   [fodsearch.level.repo :as repo]))

(def Level
  [:or
   [:map
    [:id int?]
    [:name string?]]
   nil?])

(defn- level->json
  "Convert repo result into a map that can be converted to JSON."
  [{:level/keys [id name] :as _level}]
  {:id id :name name})

(defn get-all
  "Get all of the levels."
  []
  (let [levels (repo/select-all)]
    (mapv level->json levels)))
(m/=> get-all
      [:=> :cat [:vector Level]])

(defn find-one
  "Get a single level by its value."
  [by value]
  (let [[level] (repo/select by value)]
    (if level
      (level->json level)
      nil)))
(m/=> find-one
      [:=> [:cat keyword? int?] Level])

(defn edit
  "Update a single category by its id."
  [{:keys [id] :as level}]
  (let [_ (repo/update-by-id level)]
    (find-one :id id)))

(defn create
  "Create a new level."
  [{name :name :as level}]
  (let [_ (repo/insert level)]
    (find-one :name name)))

(defn delete
  "Delete a single level by its value."
  [by value]
  (condp = by
    :id (do (repo/delete-by-id value) nil)))
