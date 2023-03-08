(ns dev.server
  (:require
   [fodsearch.rest-api.core :as api]))

(def ^:private ratom atom)

(defonce log (ratom []))

(defn- tap-fn [x]
  (swap! log conj x))

(add-tap tap-fn)

(comment
  @log
  (reset! log [])

  (api/start-server)
  (api/stop-server)
  )
