(ns fodsearch.level.repo
  (:require
   [fodsearch.database.interface :as db]))

(defn select-all
  "Select all levels."
  []
  (let [query {:select [:id :name]
               :from   [:level]}]
    (db/exec! query)))

(defn select
  "Select level(s) where `by = value`."
  [by value]
  (let [query {:select [:id :name]
               :from [:level]
               :where [:= by value]}]
    (db/exec! query)))

(defn insert
  "Insert a new level into the `level` table."
  [{:keys [name] :as _level}]
  (let [query {:insert-into [:level]
               :columns     [:name]
               :values      [[name]]}]
    (db/exec! query)))

(defn update-by-id
  "Update a level by its id."
  [{:keys [id] :as level}]
  (let [query {:update [:level]
               :set    level
               :where  [:= :id id]}]
    (db/exec! query)))

(defn delete-by-id
  "Delete a single level by its id"
  [id]
  (let [query {:delete-from [:level]
               :where       [:= :id id]}]
    (db/exec! query)))
