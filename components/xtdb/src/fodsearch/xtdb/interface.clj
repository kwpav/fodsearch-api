(ns fodsearch.xtdb.interface
  (:require [clojure.java.io :as io]))

(defonce node (xt/start-node {}))

(defn query [])

(defn create [])

(defn delete [])

(defn update [])

(comment
  (require '[malli.generator :as mg])
  ;; (require '[tech.v3.dataset :as ds])
  (require '[scicloj.ml.dataset :as ds])

  (def ingredients (ds/->dataset "resources/xtdb/ingredients.csv" {:key-fn keyword :parser-fn :string}))
  ,)
