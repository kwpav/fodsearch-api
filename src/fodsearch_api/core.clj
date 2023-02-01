(ns fodsearch-api.core
  (:require
   [fodsearch-api.domain.ingredient :as ingredient]
   [fodsearch-api.domain.type :as type]
   [fodsearch-api.domain.category :as category]
   [clojure.pprint :as pprint]
   [muuntaja.core :as mc]
   [reitit.coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.muuntaja :as mmw]
   [reitit.ring.middleware.parameters :as parameters]
   [ring.adapter.jetty :as jetty]))

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

(defn edit-ingredient-handler
  [{{{id :ingredient-id} :path
     updated-ingredient  :body} :parameters :as _request}]
  (let [ingredient (ingredient/find-one :id id)]
    (if ingredient
      {:status 200
       :body   {:ingredient (ingredient/edit (into {:id id} updated-ingredient))}}
      {:status 201
       :body   {:ingredient (ingredient/create updated-ingredient)}})))

(defn delete-ingredient-handler
  [{{{id :ingredient-id} :path} :parameters :as _request}]
  (let [ingredient (ingredient/find-one :id id)]
    (if ingredient
      {:status 204
       :body   (ingredient/delete :id id)}
      {:status 404
       :body   nil})))

(defn create-ingredient-handler
  [{{{:keys [name info category type]} :body} :parameters :as _request}]
  (let [ingredient {:name     name
                    :info     info
                    :category category
                    :type     type}]
    {:status 201
     :body   {:ingredient (ingredient/create ingredient)}}))

(defn get-types-handler
  [_request]
  (let [types (type/get-all)]
    {:status 200
     :body   {:types types}}))

(defn get-type-handler
  [{{{id :type-id} :path} :parameters :as _request}]
  (let [type (type/find-one :id id)]
    (if type
      {:status 200
       :body   {:type type}}
      {:status 404
       :body   nil})))

(defn delete-type-handler
  [{{{id :type-id} :path} :parameters :as _request}]
  (let [type (type/find-one :id id)]
    (if type
      {:status 204
       :body   (type/delete :id id)}
      {:status 404
       :body   nil})))

(defn create-type-handler
  [{{{:keys [name]} :body} :parameters :as _request}]
  (let [type {:name name}]
    {:status 201
     :body   {:type (type/create type)}}))

(defn edit-type-handler
  [{{{id :type-id} :path
     updated-type  :body} :parameters :as _request}]
  (let [type (type/find-one :id id)]
    (if type
      {:status 200
       :body   {:type (type/edit (into {:id id} updated-type))}}
      {:status 201
       :body   {:category (type/create updated-type)}})))

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

(defn edit-category-handler
  [{{{id :category-id} :path
     updated-category  :body} :parameters :as _request}]
  (let [category (category/find-one :id id)]
    (if category
      {:status 204
       :body   {:category (category/edit (into {:id id} updated-category))}}
      {:status 201
       :body   {:category (category/create updated-category)}})))

(defn delete-category-handler
  [{{{id :category-id} :path} :parameters :as _request}]
  (let [category (category/find-one :id id)]
    (if category
      {:status 204
       :body   (category/delete :id id)}
      {:status 404
       :body   nil})))

(defn create-category-handler
  [{{{:keys [name]} :body} :parameters :as _request}]
  (let [category {:name name}]
    {:status 201
     :body   {:category (category/create category)}}))

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
      ["/ingredients"
       [""
        {:get
         {:name      ::get-ingredients
          :parameters {:query [:map [:q {:optional true} string?]]}
          :responses {200 {:body [:map
                                  [:ingredients [:vector ingredient/Ingredient]]]}}
          :handler   get-ingredients-handler}
         :post
         {:name       ::create-ingredient
          :parameters {:body [:map
                              [:name string?]
                              [:category string?]
                              [:type string?]
                              [:info {:optional true} string?]]}
          :responses  {201 {:body [:map
                                   [:ingredient ingredient/Ingredient]]}}
          :handler    create-ingredient-handler}}]
       ["/:ingredient-id"
        {:get
         {:name       ::get-ingredient
          :parameters {:path [:map [:ingredient-id int?]]}
          :responses  {200 {:body [:map [:ingredient ingredient/Ingredient]]}
                       404 {:body nil?}}
          :handler    get-ingredient-handler}
         :delete
         {:name       ::delete-ingredient
          :parameters {:path [:map [:ingredient-id int?]]}
          :responses  {204 {:body nil?}
                       404 {:body nil?}}
          :handler    delete-ingredient-handler}
         :put
         {:name       ::edit-ingredient
          :parameters {:path [:map [:ingredient-id int?]]
                       :body any?}
          :response   {200 {:body [:map [:ingredient ingredient/Ingredient]]}
                       201 {:body [:map [:ingredient ingredient/Ingredient]]}}
          :handler    edit-ingredient-handler}}]]
      ["/types"
       [""
        {:get
         {:name      ::get-types
          :responses {200 {:body [:map
                                  [:types [:vector type/Type]]]}}
          :handler   get-types-handler}
         :post
         {:name       ::create-type
          :parameters {:body [:map
                              [:name string?]]}
          :responses  {201 {:body [:map
                                   [:type type/Type]]}}
          :handler    create-type-handler}}]
       ["/:type-id"
        {:get
         {:name       ::get-type
          :parameters {:path [:map [:type-id int?]]}
          :responses  {200 {:body [:map [:type type/Type]]}
                       404 {:body nil?}}
          :handler    get-type-handler}
         :delete
         {:name       ::delete-type
          :parameters {:path [:map [:type-id int?]]}
          :responses  {204 {:body nil?}
                       404 {:body nil?}}
          :handler    delete-type-handler}
         :put
         {:name       ::edit-type
          :parameters {:path [:map [:type-id int?]]
                       :body any?}
          :response   {200 {:body [:map [:type type/Type]]}
                       201 {:body [:map [:type type/Type]]}}
          :handler    edit-type-handler}}]]
      ["/categories"
       [""
        {:get
         {:name      ::get-categories
          :responses {200 {:body [:map
                                  [:categories [:vector category/Category]]]}}
          :handler   get-categories-handler}
         :post
         {:name       ::create-category
          :parameters {:body [:map
                              [:name string?]]}
          :responses  {201 {:body [:map
                                   [:category category/Category]]}}
          :handler    create-category-handler}}]
       ["/:category-id"
        {:get
         {:name       ::get-category
          :parameters {:path [:map [:category-id int?]]}
          :responses  {200 {:body [:map [:category category/Category]]}
                       404 {:body nil?}}
          :handler    get-category-handler}
         :delete
         {:name       ::delete-type
          :parameters {:path [:map [:category-id int?]]}
          :responses  {204 {:body nil?}
                       404 {:body nil?}}
          :handler    delete-category-handler}
         :put
         {:name       ::edit-category
          :parameters {:path [:map [:category-id int?]]
                       :body any?}
          :response   {200 {:body [:map [:category category/Category]]}
                       201 {:body [:map [:category category/Category]]}}
          :handler    edit-category-handler}}]]]]
    {:data {:coercion   reitit.coercion.malli/coercion
            :muuntaja   mc/instance
            :middleware [parameters/parameters-middleware
                         mmw/format-middleware
                         rrc/coerce-exceptions-middleware
                         mmw/format-request-middleware
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
  [& _args]
  (println "Starting server at localhost:3000")
  (start-server))

(comment
  (start-server)
  (stop-server)

  ;; NOTE!
  ;; Need to comment out `mmw/format-middleware` for requests with `body-params` to work
  ;; CRUD operations for ingredient(s)
  ;; CREATE
  ;; 201 response
  (app {:request-method :post
        :uri            "/api/v1/ingredients"
        :body-params    {:name     "testing343"
                         :category "fruit"
                         :type     "safe"
                         :info     "hello world"}})
  ;; READ
  ;; 200 response
   (app {:request-method :get
        :uri            "/api/v1/ingredients"})
   ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/ingredients/20"})
  ;; 404 response
  (app {:request-method :get
        :uri            "/api/v1/ingredients/999"})
  ;; UPDATE
  ;; 200 response
  (app {:request-method :put
        :uri            "/api/v1/ingredients/1"
        :body-params    {:name     "testing change2"
                         :category "fruit"
                         :type     "safe"
                         :info     "hello world"}})
  ;; DELETE
  ;; 204 response
  (app {:request-method :delete
        :uri            "/api/v1/ingredients/1"})
    ;; 404 response
  (app {:request-method :delete
        :uri            "/api/v1/ingredients/999"})

  ;; CRUD operations for types
  ;; CREATE
  ;; 201 response
  (app {:request-method :post
        :uri            "/api/v1/types"
        :body-params    {:name "testing"}})
  ;; READ
  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/types/1"})
  ;; 404 response
  (app {:request-method :get
        :uri            "/api/v1/types/100"})
  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/types"})
  ;; UPDATE
  ;; 200 response
  (app {:request-method :put
        :uri            "/api/v1/types/1"
        :body-params    {:name "testing123"}})
  ;; 201 response
  (app {:request-method :put
        :uri            "/api/v1/types/1000"
        :body-params    {:name "testing99"}})
  ;; DELETE
  ;; 500 response - cant delete used type
  (app {:request-method :delete
        :uri            "/api/v1/types/1"})
  ;; 404 response
  (app {:request-method :delete
        :uri            "/api/v1/types/999"})

  ;; CRUD operations for categories
  ;; CREATE
  ;; 201 response
  (app {:request-method :post
        :uri            "/api/v1/categories"
        :body-params    {:name "testing69"}})
  ;; READ
  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/categories"})
  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/categories/1"})
  ;; 404 response
  (app {:request-method :get
        :uri            "/api/v1/categories/100"})
  ;; UPDATE
  ;; 200 response
  (app {:request-method :put
        :uri            "/api/v1/categories/1"
        :body-params    {:name "testing123"}})
  ;; 500 response - cant delete used category
  (app {:request-method :delete
        :uri            "/api/v1/categories/1"})
  ;; 404 response
  (app {:request-method :delete
        :uri            "/api/v1/categories/999"})
  ,)
