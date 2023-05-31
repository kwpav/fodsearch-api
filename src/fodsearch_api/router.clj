(ns fodsearch-api.router
  (:require
   [fodsearch-api.api.routes :as routes]
   [fodsearch-api.middleware.app-config :as app-config]
   [muuntaja.core :as mc]
   [reitit.coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.muuntaja :as mmw]
   [reitit.ring.middleware.parameters :as parameters]))

(defn router
  [app-config]
  (ring/ring-handler
   (ring/router
    routes/routes
    {:data {:app-config app-config
            :coercion   reitit.coercion.malli/coercion
            :muuntaja   mc/instance
            :middleware [[app-config/app-config-middleware app-config]
                         parameters/parameters-middleware
                         mmw/format-middleware
                         rrc/coerce-exceptions-middleware
                         mmw/format-request-middleware
                         rrc/coerce-request-middleware
                         mmw/format-response-middleware
                         rrc/coerce-response-middleware]}})))