(ns fodsearch.rest-api.handler)

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

#_(defn edit-ingredient-handler
  [{{{id :ingredient-id} :path
     updated-ingredient  :body} :parameters :as _request}]
  (let [ingredient (ingredient/find-one :id id)]
    (if ingredient
      {:status 200
       :body   {:ingredient (ingredient/edit (into {:id id} updated-ingredient))}}
      {:status 201
       :body   {:ingredient (ingredient/create updated-ingredient)}})))

#_(defn delete-ingredient-handler
  [{{{id :ingredient-id} :path} :parameters :as _request}]
  (let [ingredient (ingredient/find-one :id id)]
    (if ingredient
      {:status 204
       :body   (ingredient/delete :id id)}
      {:status 404
       :body   nil})))

#_(defn create-ingredient-handler
  [{{{:keys [name info category level]} :body} :parameters :as _request}]
  (let [ingredient {:name     name
                    :info     info
                    :category category
                    :level    level}]
    {:status 201
     :body   {:ingredient (ingredient/create ingredient)}}))

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

#_(defn delete-level-handler
  [{{{id :level-id} :path} :parameters :as _request}]
  (let [level (level/find-one :id id)]
    (if level
      {:status 204
       :body   (level/delete :id id)}
      {:status 404
       :body   nil})))

#_(defn create-level-handler
  [{{{:keys [name]} :body} :parameters :as _request}]
  (let [level {:name name}]
    {:status 201
     :body   {:level (level/create level)}}))

#_(defn edit-level-handler
  [{{{id :level-id} :path
     updated-level  :body} :parameters :as _request}]
  (let [level (level/find-one :id id)]
    (if level
      {:status 200
       :body   {:level (level/edit (into {:id id} updated-level))}}
      {:status 201
       :body   {:category (level/create updated-level)}})))

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

#_(defn edit-category-handler
  [{{{id :category-id} :path
     updated-category  :body} :parameters :as _request}]
  (let [category (category/find-one :id id)]
    (if category
      {:status 204
       :body   {:category (category/edit (into {:id id} updated-category))}}
      {:status 201
       :body   {:category (category/create updated-category)}})))

#_(defn delete-category-handler
  [{{{id :category-id} :path} :parameters :as _request}]
  (let [category (category/find-one :id id)]
    (if category
      {:status 204
       :body   (category/delete :id id)}
      {:status 404
       :body   nil})))

#_(defn create-category-handler
  [{{{:keys [name]} :body} :parameters :as _request}]
  (let [category {:name name}]
    {:status 201
     :body   {:category (category/create category)}}))

