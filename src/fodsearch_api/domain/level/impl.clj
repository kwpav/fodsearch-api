(ns fodsearch-api.domain.level.impl
  (:require
   [fodsearch-api.database.interface :as db]))

(defn select-all
  "Select all levels."
  []
  (db/query '{:find [?id ?name]
              :keys [id name]
              :where [[?id :level/name ?name]]}))

;; TODO
;; see repo/category
(defn select
  "Select level(s) where `by = value`."
  [by value]
  (cond
    (= :id by)
    (db/query '{:find  [?name]
                :keys  [name]
                :in    [lvl-id]
                :where [[lvl-id :level/name ?name]]}
              value)
    (= :name by)
    (db/query '{:find  [?id]
                :keys  [id]
                :in    [lvl-name]
                :where [[?id :level/name cat-name]]}
              value)))
