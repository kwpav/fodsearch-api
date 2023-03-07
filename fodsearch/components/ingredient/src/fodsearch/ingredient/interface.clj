(ns fodsearch.ingredient.interface
  (:require
   [malli.core :as m]
   [fodsearch.ingredient.repo :as repo]
   [fodsearch.level.interface :as level]
   [fodsearch.category.interface :as category]))

(def Ingredient
  [:or
   [:map
    [:id int?]
    [:name string?]
    [:info {:optional true} string?]
    [:category category/Category]
    [:level level/Level]]
   nil?])

(defn- ingredient->json
  "Convert repo result into a map that can be converted to JSON."
  [{:ingredient/keys [id name info level_id category_id] :as _ingredient}]
  (let [level    (level/find-one :id level_id)
        category (category/find-one :id category_id)]
    {:id       id
     :name     name
     :info     info
     :level    level
     :category category}))

(defn get-all
  "Get all ingredients."
  []
  (let [ingredients (repo/select-all)]
    (mapv ingredient->json ingredients)))
(m/=> get-all
      [:=> :cat [:vector Ingredient]])

(defn search
  "Search for an ingredient that matches the given query string."
  [query]
  (let [ingredients (repo/search query)]
    (mapv ingredient->json ingredients)))

(defn find-one
  "Get a single ingredient by its value."
  [by value]
  (let [[ingredient] (repo/select by value)]
    (if ingredient
      (ingredient->json ingredient)
      nil)))
(m/=> find-one
      [:=> [:cat keyword? int?] Ingredient])

(defn edit
  "Update a single ingredient by its id."
  [{:keys [id name info level category] :as _ingredient}]
  (let [{level-id :id}    (level/find-one :name level)
        {category-id :id} (category/find-one :name category)
        ingredient        (into {} (remove (comp nil? val)
                                           {:id          id
                                            :name        name
                                            :info        info
                                            :level_id    level-id
                                            :category_id category-id}))
        _                 (repo/update-by-id ingredient)]
    (find-one :id id)))

(defn create
  "Create a new ingredient."
  [{:keys [name info category level] :as _ingredient}]
  (let [{level-id :id}    (level/find-one :name level)
        {category-id :id} (category/find-one :name category)
        ingredient        {:name        name
                           :info        info
                           :category-id category-id
                           :level-id    level-id}
        _                 (repo/insert ingredient)]
    (find-one :name name)))

(defn delete
  "Delete a single category by its value."
  [by value]
  (condp = by
    :id (do (repo/delete-by-id value) nil)))
