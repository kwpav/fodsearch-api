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

;; TODO
;; - pull syntax?
;; - make this a multimethod?
;; or make specific commands, e.g. get-by-id?
(defn find-by-id
  "Select category(ies) where `by = value`."
  [value {:keys [node] :as _app-config}]
  (db/query
   node
   '{:find  [?name]
     :keys  [name]
     :in    [cat-id]
     :where [[cat-id :category/name ?name]]}
   value))
