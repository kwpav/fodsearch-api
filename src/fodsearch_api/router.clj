(ns fodsearch-api.router
  (:require
   [fodsearch-api.api.routes :as routes]
   [muuntaja.core :as mc]
   [reitit.coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.muuntaja :as mmw]
   [reitit.ring.middleware.parameters :as parameters]))

(def router
  (ring/ring-handler
   (ring/router
    routes/routes
    {:data {:coercion   reitit.coercion.malli/coercion
            :muuntaja   mc/instance
            :middleware [parameters/parameters-middleware
                         mmw/format-middleware
                         rrc/coerce-exceptions-middleware
                         mmw/format-request-middleware
                         rrc/coerce-request-middleware
                         mmw/format-response-middleware
                         rrc/coerce-response-middleware]}})))
