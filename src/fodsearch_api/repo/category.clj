(ns fodsearch-api.repo.category
  (:require
   [fodsearch-api.db.db :as db]))

(defn select-all
  "Select all categories."
  []
  (db/query '{:find  [?id ?name]
              :keys  [id name]
              :where [[?id :category/name ?name]]})
  #_(let [query {:select [:id :name]
                 :from   [:category]}]
      (db/exec! query)))

;; TODO
;; - pull syntax?
;; - make this a multimethod?
;; or make specific commands, e.g. get-by-id?
(defn select
  "Select category(ies) where `by = value`."
  [by value]
  (cond
    (= :id by)
    (db/query '{:find  [?name]
                :keys  [name]
                :in    [cat-id]
                :where [[cat-id :category/name ?name]]}
              value)
    (= :name by)
    (db/query '{:find  [?id]
                :keys  [id]
                :in    [cat-name]
                :where [[?id :category/name cat-name]]}
              value)))
