(ns fodsearch.xtdb.interface
  (:require [clojure.java.io :as io]))

(def IngredientCsv
  [:map
   [:name string?]
   [:info {:optional true} string?]
   [:category string?]
   [:level [:enum "safe" "moderate"]]])

(comment
  (require '[malli.generator :as mg])
  ;; (require '[tech.v3.dataset :as ds])
  (require '[scicloj.ml.dataset :as ds])

  (def ingredients (ds/->dataset "resources/xtdb/ingredients.csv" {:key-fn keyword :parser-fn :string}))
  ,)
