(ns fodsearch-api.domain.category.impl
  (:require
   [fodsearch-api.database.interface :as db]))

(defn select-all
  "Select all categories."
  [{:keys [node] :as _app-config}]
  (db/query
   node
   '{:find  [?id ?name]
     :keys  [id name]
     :where [[?id :category/name ?name]]}))

(defn find-by-id
  "Select category(ies) where `by = value`."
  [id {:keys [node] :as _app-config}]
  (db/query
   node
   '{:find  [?id ?name]
     :keys  [id name]
     :in    [cat-id]
     :where [[?id :category/name ?name]
             [(= ?id cat-id)]]}
   id))
