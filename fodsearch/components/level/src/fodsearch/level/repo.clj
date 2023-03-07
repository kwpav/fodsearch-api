(ns fodsearch.level.repo
  (:require
   [fodsearch.database.interface :as db]))

(defn select-all
  "Select all types."
  []
  (let [query {:select [:id :name]
               :from   [:type]}]
    (db/exec! query)))

(defn select
  "Select type(s) where `by = value`."
  [by value]
  (let [query {:select [:id :name]
               :from [:type]
               :where [:= by value]}]
    (db/exec! query)))

(defn insert
  "Insert a new type into the `type` table."
  [{:keys [name] :as _type}]
  (let [query {:insert-into [:type]
               :columns     [:name]
               :values      [[name]]}]
    (db/exec! query)))

(defn update-by-id
  "Update a type by its id."
  [{:keys [id] :as type}]
  (let [query {:update [:type]
               :set    type
               :where  [:= :id id]}]
    (db/exec! query)))

(defn delete-by-id
  "Delete a single type by its id"
  [id]
  (let [query {:delete-from [:type]
               :where       [:= :id id]}]
    (db/exec! query)))
