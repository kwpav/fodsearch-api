(ns fodsearch-api.core
  (:require
   [fodsearch-api.domain :as domain]
   [fodsearch-api.repo :as repo]
   [muuntaja.core :as mc]
   [reitit.coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.muuntaja :as mmw]
   [reitit.ring.middleware.parameters :as parameters]
   [ring.adapter.jetty :as jetty]))

(defn ingredient-handler
  [{{{id :ingredient-id} :path} :parameters :as _request}]
  (let [ingredient (domain/get-ingredient :id id)]
    (if ingredient
      {:status 200
       :body   {:ingredient ingredient}}
      {:status 404
       :body   nil})))

(defn ingredients-handler
  [_request]
  (let [ingredients (domain/get-ingredients)]
    {:status 200
     :body   {:ingredients ingredients}}))

(defn type-handler
  [{{{id :type-id} :path} :parameters :as _request}]
  (let [type (domain/get-type :id id)]
    (if type
      {:status 200
       :body   {:type type}}
      {:status 404
       :body   nil})))

(defn types-handler
  [_request]
  (let [types (domain/get-types)]
    {:status 200
     :body   {:types types}}))

(defn category-handler
  [{{{id :category-id} :path} :parameters :as _request}]
  (let [category (domain/get-category :id id)]
    (if category
      {:status 200
       :body   {:category category}}
      {:status 404
       :body   nil})))

(defn categories-handler
  [_request]
  (let [categories (domain/get-categories)]
    {:status 200
     :body   {:categories categories}}))

(def app
  (ring/ring-handler
   (ring/router
    ["/api"
     ["/v1"
      ["/health"
       {:get
        {:name      ::health
         :responses {200 {:body [:map [:healthy boolean?]]}}
         :handler   (fn [_] {:status 200
                             :body   {:healthy true}})}}]
      ["/ingredients/:ingredient-id"
       {:get
        {:name       ::ingredient
         :parameters {:path [:map [:ingredient-id int?]]}
         :responses  {200 {:body [:map [:ingredient domain/Ingredient]]}
                      404 {:body nil?}}
         :handler    ingredient-handler}}]
      ["/ingredients"
       {:get
        {:name      ::ingredients
         :responses {200 {:body [:map
                                 [:ingredients [:vector domain/Ingredient]]]}}
         :handler   ingredients-handler}}]
      ["/types"
       {:get
        {:name      ::types
         :responses {200 {:body [:map
                                 [:types [:vector domain/Type]]]}}
         :handler   types-handler}}]
      ["/types/:type-id"
       {:get
        {:name       ::type
         :parameters {:path [:map [:type-id int?]]}
         :responses  {200 {:body [:map [:type domain/Type]]}
                      404 {:body nil?}}
         :handler    type-handler}}]
      ["/categories"
       {:get
        {:name      ::categories
         :responses {200 {:body [:map
                                 [:categories [:vector domain/Category]]]}}
         :handler   categories-handler}}]
      ["/categories/:category-id"
       {:get
        {:name       ::category
         :parameters {:path [:map [:category-id int?]]}
         :responses  {200 {:body [:map [:category domain/Category]]}
                      404 {:body nil?}}
         :handler    category-handler}}]]]
    {:data {:coercion   reitit.coercion.malli/coercion
            :muuntaja   mc/instance
            :middleware [parameters/parameters-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-request-middleware
                         mmw/format-response-middleware
                         rrc/coerce-response-middleware]}})))

(defonce server (jetty/run-jetty #'app {:port 3000 :join? false}))

(defn start-server
  []
  (.start server))

(defn stop-server
  []
  (.stop server))

(defn -main
  [& args]
  (println "Starting server at localhost:3000")
  (start-server))

(comment
  (start-server)
  (stop-server)

  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/ingredients/1"})
  ;; 404 response
  (app {:request-method :get
        :uri            "/api/v1/ingredients/100"})
  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/ingredients"})

  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/types/1"})
  ;; 404 response
  (app {:request-method :get
        :uri            "/api/v1/types/100"})
  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/types"})

  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/categories/1"})
  ;; 404 response
  (app {:request-method :get
        :uri            "/api/v1/categories/100"})
  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/categories"})
  ,)
