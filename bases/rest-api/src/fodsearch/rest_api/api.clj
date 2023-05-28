(ns fodsearch.rest-api.api
  (:require
   [fodsearch.category.interface :as category]
   [fodsearch.ingredient.interface :as ingredient]
   [fodsearch.level.interface :as level]
   [fodsearch.rest-api.handler :as handler]
   [muuntaja.core :as mc]
   [reitit.coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.muuntaja :as mmw]
   [reitit.ring.middleware.parameters :as parameters]))

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
        :parameters {:path [:map [:ingredient-id int?]]}
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
        :parameters {:path [:map [:level-id int?]]}
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
        :parameters {:path [:map [:category-id int?]]}
        :responses  {200 {:body [:map [:category category/Category]]}
                     404 {:body nil?}}
        :handler    handler/get-category-handler}}]]]])

(def app
  (ring/ring-handler
   (ring/router
    routes
    {:data {:coercion   reitit.coercion.malli/coercion
            :muuntaja   mc/instance
            :middleware [parameters/parameters-middleware
                         mmw/format-middleware
                         rrc/coerce-exceptions-middleware
                         mmw/format-request-middleware
                         rrc/coerce-request-middleware
                         mmw/format-response-middleware
                         rrc/coerce-response-middleware]}})))


