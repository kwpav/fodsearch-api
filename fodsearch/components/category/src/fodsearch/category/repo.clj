(ns fodsearch.category.repo
  (:require
   [fodsearch.database.interface :as db]))

(defn select-all
  "Select all categories."
  []
  (let [query {:select [:id :name]
               :from   [:category]}]
    (db/exec! query)))

(defn select
  "Select category(ies) where `by = value`."
  [by value]
  (let [query {:select [:id :name]
               :from   [:category]
               :where  [:= by value]}]
    (db/exec! query)))

(defn insert
  "Insert a new category into the `category` table."
  [{:keys [name] :as _category}]
  (let [query {:insert-into [:category]
               :columns     [:name]
               :values      [[name]]}]
    (db/exec! query)))

(defn update-by-id
  "Update a category by its id."
  [{:keys [id] :as category}]
  (let [query {:update [:category]
               :set    category
               :where  [:= :id id]}]
    (db/exec! query)))

(defn delete-by-id
  "Delete a category type by its id"
  [id]
  (let [query {:delete-from [:category]
               :where       [:= :id id]}]
    (db/exec! query)))
