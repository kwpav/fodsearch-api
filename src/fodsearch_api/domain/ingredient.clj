(ns fodsearch-api.domain.ingredient
  (:require
   [malli.core :as m]
   [fodsearch-api.repo.ingredient :as repo]
   [fodsearch-api.domain.type :as type]
   [fodsearch-api.domain.category :as category]
   [fodsearch-api.domain.ingredient :as ingredient]))

(def Ingredient
  [:or
   [:map
    [:id int?]
    [:name string?]
    [:info {:optional true} string?]
    [:category category/Category]
    [:type type/Type]]
   nil?])

(defn- ingredient->json
  "Convert repo result into a map that can be converted to JSON."
  [{:ingredient/keys [id name info type_id category_id] :as _ingredient}]
  (let [type     (type/find-one :id type_id)
        category (category/find-one :id category_id)]
    {:id       id
     :name     name
     :info     info
     :type     type
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
  [{:keys [id name info type category] :as _ingredient}]
  (let [{type-id :id}     (type/find-one :name type)
        {category-id :id} (category/find-one :name category)
        ingredient        (into {} (remove (comp nil? val)
                                           {:id          id
                                            :name        name
                                            :info        info
                                            :type_id     type-id
                                            :category_id category-id}))
        _                 (repo/update-by-id ingredient)]
    (find-one :id id)))

(defn create
  "Create a new ingredient."
  [{:keys [name info category type] :as _ingredient}]
  (let [{type-id :id}     (type/find-one :name type)
        {category-id :id} (category/find-one :name category)
        ingredient        {:name        name
                           :info        info
                           :category-id category-id
                           :type-id     type-id}
        _                 (repo/insert ingredient)]
    (find-one :name name)))

(defn delete
  "Delete a single category by its value."
  [by value]
  (condp = by
    :id (do (repo/delete-by-id value) nil)))
