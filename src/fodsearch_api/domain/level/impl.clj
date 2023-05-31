(ns fodsearch-api.domain.level.impl
  (:require
   [fodsearch-api.database.interface :as db]))

(defn get-all
  "Select all levels."
  [{:keys [node] :as _app-config}]
  (db/query
   node
   '{:find [?id ?name]
     :keys [id name]
     :where [[?id :level/name ?name]]}))

;; TODO
;; see repo/category
(defn find-by-id
  "Select level(s) where `by = value`."
  [value {:keys [node] :as _app-config}]
  (db/query
   node
   '{:find  [?name]
     :keys  [name]
     :in    [lvl-id]
     :where [[lvl-id :level/name ?name]]}
   value))
