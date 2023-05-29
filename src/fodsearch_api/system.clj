(ns fodsearch-api.system
  (:require
   [clojure.tools.logging :as logging]
   [donut.system :as ds]
   [fodsearch-api.api :as api]
   [ring.adapter.jetty :as jetty]
   [xtdb.api :as xt]))

#_(def server
    #::ds{:start (fn [{:keys [::ds/config]}]
                   (let [{:keys [handler options]} config]
                     (jetty/run-jetty handler options)))
          :stop (fn [{:keys [::ds/instance]}]
                  (.stop instance))
          :config {:handler (ds/local-ref [:handler])
                   :options {:port (ds/local-ref [:port])
                             :join? false}
                   #_#_:node (ds/ref [:rest-api :xt-node])}})

#_(def service
  ;; TODO api/app goes here
  ;; inject node here?
    #::ds{:start (fn [])
          :stop (fn [])})

#_(def xt-node
  ;; signal handlers for xt-node
    #::ds{:start (fn []
                   (xt/start-node {}))
          :stop  (fn [{:keys [::ds/instance]}]
                   (.close instance))})

(def base-system
  {::ds/defs
   {:rest-api ;; component group
    {:server  ;; component name
     #::ds{:start  (fn start-server
                     [{{:keys [service options]} ::ds/config}]
                    (jetty/run-jetty #'api/app options))
           :stop   (fn stop-server
                     [{::ds/keys [instance] :as s}]
                     (tap> s)
                     (.stop instance))
           :config {:service (ds/ref [:rest-api :service])
                    :options     {:port  3000
                                  :join? false}}}

     :service
     #::ds{:start (fn start-service
                    [_]
                    #'api/app)}

     :xt-node
     #::ds{:start (fn start-node
                    [_]
                    (xt/start-node {}))
           :stop  (fn stop-node
                    [{:keys [::ds/instance]}]
                    (.close instance))}}}})

(comment

  (def running-system (ds/start base-system))
  (ds/stop base-system)
  ;; or...
  (def running-system (ds/signal base-system ::ds/start))
  (ds/signal running-system ::ds/stop)


  ;; system ns example
  ;; Use aero for all configuration
  (defn env-config [& [profile]]
    (aero/read-config (io/resource "config/env.edn")
                      (when profile {:profile profile})))

;; define all behavior in base-system
  (def base-system
    {::ds/defs
     {:env {}

      :http
      {:server
       #::ds{:start  (fn [{{:keys [handler options]} ::ds/config}]
                       (rj/run-jetty handler options))
             :stop   (fn [{::ds/keys [instance]}]
                       (.stop instance))
             :config {:handler (ds/ref [:http :handler])
                      :options {:port  (ds/ref [:env :http-port])
                                :join? false}}}

       :handler
       #::ds{:start (fn [_]
                    ;; handler goes here
                      )}}}})

  (defmethod ds/named-system :base
    [_]
    base-system)

  (defmethod ds/named-system :dev
    [_]
    (ds/system :base {[:env] (env-config :dev)}))

  (defmethod ds/named-system :donut.system/repl
    [_]
    (ds/system :dev))

  (defmethod ds/named-system :test
    [_]
    (ds/system :dev
               {[:http :server] ::disabled}))

;; double server example
  (def HTTPServer
    #::ds{:start  (fn [{:keys [::ds/config]}]
                    (let [{:keys [handler options]} config]
                      (rj/run-jetty handler options)))
          :stop   (fn [{:keys [::ds/instance]}]
                    (.stop instance))
          :config {:handler (ds/local-ref [:handler])
                   :options {:port  (ds/local-ref [:port])
                             :join? false}}})

  (def system
    {::ds/defs
     {:http-1 {:server  HTTPServer
               :handler (fn [_req]
                          {:status  200
                           :headers {"ContentType" "text/html"}
                           :body    "http server 1"})
               :port    8080}

      :http-2 {:server  HTTPServer
               :handler (fn [_req]
                          {:status  200
                           :headers {"ContentType" "text/html"}
                           :body    "http server 2"})
               :port    9090}}})

;; single server example
  (def system
    {::ds/defs
     {:env  {:http-port 8080}
      :http {:server  #::ds{:start  (fn [{:keys [::ds/config]}]
                                      (let [{:keys [handler options]} config]
                                        (rj/run-jetty handler options)))
                            :stop   (fn [{:keys [::ds/instance]}]
                                      (.stop instance))
                            :config {:handler (ds/local-ref [:handler])
                                     :options {:port  (ds/ref [:env :http-port])
                                               :join? false}}}
             :handler (fn [_req]
                        {:status  200
                         :headers {"ContentType" "text/html"}
                         :body    "It's donut.system, baby!"})}}})

  (def base-system
    {::ds/defs
     {:env
      {:http {:port 8080}}

      :http
      {:server
       #::ds{:start  (fn [{:keys [handler options]}]
                       (rj/run-jetty handler options))
             :config {:handler (ds/local-ref [:handler])
                      :options {:port  (ds/ref [:env :http :port])
                                :join? false}}}}}})

  :comment)
