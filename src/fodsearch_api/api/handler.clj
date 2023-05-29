(ns fodsearch-api.api.handler
  (:require
   [fodsearch-api.domain.category :as category]
   [fodsearch-api.domain.ingredient :as ingredient]
   [fodsearch-api.domain.level :as level]))

(defn get-ingredients-handler
  [{{{q :q} :query} :parameters :as _request}]
  (let [ingredients (if q (ingredient/search q) (ingredient/get-all))]
    {:status 200
     :body   {:ingredients ingredients}}))

(defn get-ingredient-handler
  [{{{id :ingredient-id} :path} :parameters :as _request}]
  (let [ingredient (ingredient/find-one :id id)]
    (if ingredient
      {:status 200
       :body   {:ingredient ingredient}}
      {:status 404
       :body   nil})))

(defn get-levels-handler
  [_request]
  (let [levels (level/get-all)]
    {:status 200
     :body   {:levels levels}}))

(defn get-level-handler
  [{{{id :level-id} :path} :parameters :as _request}]
  (let [level (level/find-one :id id)]
    (if level
      {:status 200
       :body   {:level level}}
      {:status 404
       :body   nil})))

(defn get-categories-handler
  [_request]
  (let [categories (category/get-all)]
    {:status 200
     :body   {:categories categories}}))

(defn get-category-handler
  [{{{id :category-id} :path} :parameters :as _request}]
  (let [category (category/find-one :id id)]
    (if category
      {:status 200
       :body   {:category category}}
      {:status 404
       :body   nil})))
