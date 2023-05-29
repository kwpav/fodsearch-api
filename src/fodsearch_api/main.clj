(ns fodsearch-api.main
  (:require
   [donut.system :as ds]
   [fodsearch-api.api :as api]
   [ring.adapter.jetty :as jetty])
  (:gen-class))

(def ^:private server-ref (atom nil))

;; https://github.com/donut-party/system/blob/main/dev/donut/examples/multiple_http_servers.clj
(def APIServer
  #::ds{:start  (fn [{:keys [::ds/config]}]
                  (let [{:keys [handler options]} config]
                    (jetty/run-jetty handler options)))
        :stop   (fn [{:keys [::ds/instance]}]
                  (.stop instance))
        :config {:handler (ds/local-ref [:handler])
                 :options {:port  (ds/local-ref [:port])
                           :join? false}}})

#_(defonce server (jetty/run-jetty #'api/app {:port 3000 :join? false}))

#_(defn start-server
    []
    (.start server))

#_(defn stop-server
    []
    (.stop server))

#_(defn -main
    [& _args]
    (println "Starting server at localhost:3000")
    (start-server))

#_(comment
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
          :uri            "/api/v1/categories/999"}))

