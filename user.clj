(ns user
  (:require
   [donut.system :as ds]
   [fodsearch-api.database.init :as init]
   [fodsearch-api.database.interface :as db]
   [fodsearch-api.system :as system]
   [xtdb.api :as xt]))

(defonce log (atom []))

(defn- tap-fn [x]
  (swap! log conj x))

(add-tap tap-fn)

(comment
  (->> @log
       second
       #_keys
       #_:donut.system/component-id)

  (def running-system (ds/start system/base-system))
  (ds/stop running-system)

  (def node (-> running-system
                :donut.system/instances
                :rest-api
                :node))

  (xt/submit-tx node [[::xt/put {:xt/id :test :hello "world"}]])

  (db/init-db node)

  (db/query node  '{:find  [?test]
                    :where [[?test :hello ?name]]})
  (xt/sync node)

  (def ingredients (init/csv->ingredients (init/read-ingredients-csv)))

  (let [data* (into [] ingredients)]
    (for [[k v] data*]
      (cond
        (map? v) (xt/submit-tx node [[::xt/put v]])
        (vector? v) (for [datum v]
                      (xt/submit-tx node [[::xt/put datum]])))))

  (xt/sync node)

  (reset! log [])
  ,)
