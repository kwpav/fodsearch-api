(ns fodsearch.xtdb.interface
  (:require [clojure.java.io :as io]
            [xtdb.api :as xt]))

(defonce node (xt/start-node {}))

(def submit-tx xt/submit-tx)

(def query
  (partial xt/q (xt/db node)))
