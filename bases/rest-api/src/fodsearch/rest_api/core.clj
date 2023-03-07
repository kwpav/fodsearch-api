(ns fodsearch.rest-api.core
  (:require
   [fodsearch.ingredient.interface :as ingredient]
   [fodsearch.level.interface :as level]
   [fodsearch.category.interface :as category]
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
  [{{{:keys [name info category level]} :body} :parameters :as _request}]
  (let [ingredient {:name     name
                    :info     info
                    :category category
                    :level     level}]
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

(defn delete-level-handler
  [{{{id :level-id} :path} :parameters :as _request}]
  (let [level (level/find-one :id id)]
    (if level
      {:status 204
       :body   (level/delete :id id)}
      {:status 404
       :body   nil})))

(defn create-level-handler
  [{{{:keys [name]} :body} :parameters :as _request}]
  (let [level {:name name}]
    {:status 201
     :body   {:level (level/create level)}}))

(defn edit-level-handler
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
                              [:level string?]
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
      ["/levels"
       [""
        {:get
         {:name      ::get-levels
          :responses {200 {:body [:map
                                  [:levels [:vector level/Level]]]}}
          :handler   get-levels-handler}
         :post
         {:name       ::create-level
          :parameters {:body [:map
                              [:name string?]]}
          :responses  {201 {:body [:map
                                   [:level level/Level]]}}
          :handler    create-level-handler}}]
       ["/:level-id"
        {:get
         {:name       ::get-level
          :parameters {:path [:map [:level-id int?]]}
          :responses  {200 {:body [:map [:level level/Level]]}
                       404 {:body nil?}}
          :handler    get-level-handler}
         :delete
         {:name       ::delete-level
          :parameters {:path [:map [:level-id int?]]}
          :responses  {204 {:body nil?}
                       404 {:body nil?}}
          :handler    delete-level-handler}
         :put
         {:name       ::edit-level
          :parameters {:path [:map [:level-id int?]]
                       :body any?}
          :response   {200 {:body [:map [:level level/Level]]}
                       201 {:body [:map [:level level/Level]]}}
          :handler    edit-level-handler}}]]
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
         {:name       ::delete-level
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
                         :level     "safe"
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
                         :level     "safe"
                         :info     "hello world"}})
  ;; DELETE
  ;; 204 response
  (app {:request-method :delete
        :uri            "/api/v1/ingredients/1"})
    ;; 404 response
  (app {:request-method :delete
        :uri            "/api/v1/ingredients/999"})

  ;; CRUD operations for levels
  ;; CREATE
  ;; 201 response
  (app {:request-method :post
        :uri            "/api/v1/levels"
        :body-params    {:name "testing"}})
  ;; READ
  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/levels/1"})
  ;; 404 response
  (app {:request-method :get
        :uri            "/api/v1/levels/100"})
  ;; 200 response
  (app {:request-method :get
        :uri            "/api/v1/levels"})
  ;; UPDATE
  ;; 200 response
  (app {:request-method :put
        :uri            "/api/v1/levels/1"
        :body-params    {:name "testing123"}})
  ;; 201 response
  (app {:request-method :put
        :uri            "/api/v1/levels/1000"
        :body-params    {:name "testing99"}})
  ;; DELETE
  ;; 500 response - cant delete used level
  (app {:request-method :delete
        :uri            "/api/v1/levels/1"})
  ;; 404 response
  (app {:request-method :delete
        :uri            "/api/v1/levels/999"})

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
