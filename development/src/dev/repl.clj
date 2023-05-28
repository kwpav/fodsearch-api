(ns dev.repl)

(defonce log (atom []))

(defn- tap-fn [x]
  (swap! log conj x))

(add-tap tap-fn)

(comment
  @log
  (reset! log [])

  :end)

(require '[xtdb.node :as xt.node]
         '[xtdb.datalog :as xt])


(comment
  ;; Confirm this API call returns successfully
(def my-node (xt.node/start-node {:xtdb/server {:port 3001}
                                  :xtdb/pgwire {:port 5432}}))

  (xt/status my-node)
  (xt/submit-tx my-node [[:put :posts {:xt/id   1234
                                       :user-id 5678
                                       :text    "hello world!"}]])

  (xt/q my-node '{:find  [text]
                  :where [($ :posts [text])]})

  )
