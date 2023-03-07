(ns fodsearch.ingredient.repo
  (:require
   [fodsearch.database.interface :as db]))

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
               :from   [:ingredient]
               :where  [:= by value]}]
    (db/exec! query)))

(defn search
  "Search for ingredients with the given query string.
  This is case-insensitive and will search for ingredients where
  some part of the name, category, or level matches of the query string."
  [q]
  (let [query-string (str "%" q "%")
        category-ids {:select [:id]
                      :from   [:category]
                      :where  [:ilike :name query-string]}
        level-ids     {:select [:id]
                      :from   [:level]
                      :where  [:ilike :name query-string]}
        query        {:select [:id :name :info :category_id :level_id]
                      :from   [:ingredient]
                      :where  [:or
                               [:ilike :name query-string]
                               [:in :category_id category-ids]
                               [:in :level_id level-ids]]}]
    (db/exec! query)))

(defn insert
  "Insert a new ingredient into the `ingredient` table."
  [{:keys [name info category-id level-id] :as _ingredient}]
  (let [query {:insert-into [:ingredient]
               :columns     [:name :info :category_id :level_id]
               :values      [[name info category-id level-id]]}]
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
