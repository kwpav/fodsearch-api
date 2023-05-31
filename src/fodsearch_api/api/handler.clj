(ns fodsearch-api.api.handler
  (:require
   [fodsearch-api.domain.category.interface :as category]
   [fodsearch-api.domain.ingredient.interface :as ingredient]
   [fodsearch-api.domain.level.interface :as level]))

(defn get-ingredients-handler
  [{{{q :q} :query} :parameters
    app-config      :app-config
    :as             _request}]
  (tap> {::get-ingredients-handler _request})
  (let [ingredients (if q
                      (ingredient/search q app-config)
                      (ingredient/get-all app-config))]
    {:status 200
     :body   {:ingredients ingredients}}))

(defn get-ingredient-handler
  [{{{id :ingredient-id} :path} :parameters
    app-config                  :app-config
    :as                         _request}]
  (let [ingredient (ingredient/find-by-id id app-config)]
    (if ingredient
      {:status 200
       :body   {:ingredient ingredient}}
      {:status 404
       :body   nil})))

(defn get-levels-handler
  [{:keys [app-config] :as _request}]
  (let [levels (level/get-all app-config)]
    {:status 200
     :body   {:levels levels}}))

(defn get-level-handler
  [{{{id :level-id} :path} :parameters
    app-config             :app-config
    :as                    _request}]
  (let [level (level/find-by-id id app-config)]
    (if level
      {:status 200
       :body   {:level level}}
      {:status 404
       :body   nil})))

(defn get-categories-handler
  [{:keys [app-config] :as _request}]
  (let [categories (category/get-all app-config)]
    {:status 200
     :body   {:categories categories}}))

(defn get-category-handler
  [{{{id :category-id} :path} :parameters
    app-config                :app-config
    :as                       _request}]
  (let [category (category/find-by-id id app-config)]
    (if category
      {:status 200
       :body   {:category category}}
      {:status 404
       :body   nil})))
