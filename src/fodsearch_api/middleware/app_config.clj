(ns fodsearch-api.middleware.app-config)

(defn ^:private with-app-config
  [handler app-config]
  (fn [request]
    (handler (assoc request :app-config app-config))))

(def app-config-middleware
  {:name ::transaction
   :description "Adds 'app-config' to each request/response."
   :wrap with-app-config})
