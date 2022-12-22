(ns fodsearch-api.domain
  (:require
   [fodsearch-api.repo :as repo]
   [fodsearch-api.schema :as schema]
   [malli.core :as m]
   [malli.util :as mu]))

(def Type
  [:map
   [:id int?]
   [:name schema/Type]])

(defn- type->json
  [{:type/keys [id name] :as _type}]
  {:id id :name name})

(m/=> get-types
      [:=> :cat [:vector Type]])
(defn get-types
  "Get all of the types."
  []
  (let [types (repo/get-types)]
    (mapv type->json types)))

(m/=> get-type
      [:=> [:cat keyword? int?] Type])
(defn get-type
  "Get a single type by its value."
  [by value]
  (let [type (condp = by
               :id (repo/get-type-by-id value))]
    (if type
      (type->json type)
      nil)))

(def Category
  [:map
   [:id int?]
   [:name schema/Category]])

(defn- category->json
  [{:category/keys [id name] :as _category}]
  {:id id :name name})

(m/=> get-categories
        [:=> :cat [:vector Category]])
(defn get-categories
  "Get all the categories."
  []
  (let [categories (repo/get-categories)]
    (mapv category->json categories)))

(m/=> get-category
      [:=> [:cat keyword? int?] Category])
(defn get-category
  "Get a single category by its value."
  [by value]
  (let [category (condp = by
                   :id (repo/get-category-by-id value))]
    (if category
      (category->json category)
      nil)))

(def Ingredient
  [:map
   [:id int?]
   [:info string?]
   [:category Category]
   [:type Type]]
  #_(-> schema/Ingredient
      (mu/update-properties assoc :id int?)
      (mu/update-properties :type Type)
      (mu/update-properties :category Category)))

(defn- ingredient->json
  [{:ingredient/keys [id name info type_id category_id] :as _ingredient}]
  (let [type     (get-type :id type_id)
        category (get-category :id category_id)]
    {:id       id
     :name     name
     :info     info
     :type     type
     :category category}))

(m/=> get-ingredients
      [:=> :cat [:vector Ingredient]])
(defn get-ingredients
  "Get all ingredients."
  []
  (let [ingredients (repo/get-ingredients)]
    (mapv ingredient->json ingredients)))

(m/=> get-ingredient
      [:=> [:cat keyword? int?] Ingredient])
(defn get-ingredient
  "Get a single ingredient by its value."
  [by value]
  (let [ingredient (condp = by
                     :id (repo/get-ingredient-by-id value))]
    (if ingredient
      (ingredient->json ingredient)
      nil)))

(comment
  (get-types)
  (get-type :id 1000)

  (get-categories)
  (get-category :id 1)

  (get-ingredients)
  (get-ingredient :id 100)
  ,)
