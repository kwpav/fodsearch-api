(ns fodsearch-api.repo.ingredient
  (:require
   [fodsearch-api.db.db :as db]))

(defn select-all
  "Select all ingredients."
  []
  (let [query {:select [:*]
               :from   [:ingredient]}]
    (db/exec! query)))

(defn select
  "Select ingredient(s) where `by = value`."
  [by value]
  (let [query {:select [:*]
               :from [:ingredient]
               :where [:= by value]}]
    (db/exec! query)))

(defn insert
  "Insert a new ingredient into the `ingredient` table."
  [{:keys [name info category-id type-id] :as _ingredient}]
  (let [query {:insert-into [:ingredient]
               :columns [:name :info :category_id :type_id]
               :values [[name info category-id type-id]]}]
    (db/exec! query)))

(defn update-by-id
  "Update an ingredient by its id."
  [{:keys [id] :as ingredient}]
  (let [query {:update [:ingredient]
               :set    ingredient
               :where  [:= :id id]}]
    (db/exec! query)))

(defn delete-by-id
  "Delete a single ingredient by its id"
  [id]
  (let [query {:delete-from [:ingredient]
               :where       [:= :id id]}]
    (db/exec! query)))

