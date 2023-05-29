(ns fodsearch-api.api.routes
  (:require
   [fodsearch-api.api.handler :as handler]
   [fodsearch-api.domain.category :as category]
   [fodsearch-api.domain.ingredient :as ingredient]
   [fodsearch-api.domain.level :as level]))

(def routes
  ["/api"
   ["/v1"
    ["/health"
     {:get
      {:name      ::health
       :responses {200 {:body [:map [:healthy boolean?]]}}
       :handler   (fn [_] {:status 200
                           :body   {:healthy true}})}}]
    ["/ingredients"
     [""
      {:get
       {:name      ::get-ingredients
        :parameters {:query [:map [:q {:optional true} string?]]}
        :responses {200 {:body [:map
                                [:ingredients [:vector ingredient/Ingredient]]]}}
        :handler   handler/get-ingredients-handler}}]
     ["/:ingredient-id"
      {:get
       {:name       ::get-ingredient
        :parameters {:path [:map [:ingredient-id uuid?]]}
        :responses  {200 {:body [:map [:ingredient ingredient/Ingredient]]}
                     404 {:body nil?}}
        :handler    handler/get-ingredient-handler}}]]
    ["/levels"
     [""
      {:get
       {:name      ::get-levels
        :responses {200 {:body [:map
                                [:levels [:vector level/Level]]]}}
        :handler   handler/get-levels-handler}}]
     ["/:level-id"
      {:get
       {:name       ::get-level
        :parameters {:path [:map [:level-id uuid?]]}
        :responses  {200 {:body [:map [:level level/Level]]}
                     404 {:body nil?}}
        :handler    handler/get-level-handler}}]]

    ["/categories"
     [""
      {:get
       {:name      ::get-categories
        :responses {200 {:body [:map
                                [:categories [:vector category/Category]]]}}
        :handler   handler/get-categories-handler}}]
     ["/:category-id"
      {:get
       {:name       ::get-category
        :parameters {:path [:map [:category-id uuid?]]}
        :responses  {200 {:body [:map [:category category/Category]]}
                     404 {:body nil?}}
        :handler    handler/get-category-handler}}]]]])

