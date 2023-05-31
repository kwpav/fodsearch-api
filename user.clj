(ns user)

(defonce log (atom []))

(defn- tap-fn [x]
  (swap! log conj x))

(add-tap tap-fn)

(comment
  (->> @log
       second
       #_keys
       #_:donut.system/component-id)

  (reset! log [])
  ,)
