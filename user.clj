(ns user)

(defonce log (atom []))

(defn- tap-fn [x]
  (swap! log conj x))

(add-tap tap-fn)

(comment
  (->> @log
       first
       #_keys
       :donut.system/component-id)

  (reset! log [])
  ,)
