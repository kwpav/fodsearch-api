(ns fodsearch-api.core
  (:require
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
  {:status 200
   :body   {:ingredient (repo/get-ingredient-by-id id)}})

(defn ingredients-handler
  [_request]
  {:status 200
   :body {:ingredients (repo/get-ingredients)}})

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
         :responses  {200 {:body [:map [:ingredient repo/Ingredient]]}}
         :handler    ingredient-handler}}]
      ["/ingredients"
       {:get
        {:name      ::ingredients
         :responses {200 {:body [:map
                                 [:ingredients [:vector repo/Ingredient]]]}}
         :handler   ingredients-handler}}]]]
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
  (ingredient-handler nil)

  (app {:request-method :get
        :uri            "/api/v1/ingredients/1"})

  (app {:request-method :get
        :uri            "/api/v1/ingredients"})
  ,)
