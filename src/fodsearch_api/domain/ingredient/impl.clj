(ns fodsearch-api.domain.ingredient.impl
  (:require
   [fodsearch-api.database.interface :as db]))

(defn get-all
  "Select all ingredients."
  [{:keys [node] :as _app-config}]
  (mapv first
        (db/query
         node
         '{:find  [(pull ?ingredient [(:ingredient/id {:as :id})
                                      (:ingredient/name {:as :name})
                                      (:ingredient/info {:as :info})
                                      {(:id/level {:as :level})
                                       [(:level/id {:as :id})
                                        (:level/name {:as :name})]}
                                      {(:id/category {:as :category})
                                       [(:category/id {:as :id})
                                        (:category/name {:as :name})]}])]
           :where [[?ingredient :ingredient/name ?name]]})))

(defn find-by-id
  "Select ingredient by id."
  [id {:keys [node] :as _app-config}]
  (first
   (first
    (db/query
     node
     '{:find  [(pull ?ingredient [(:ingredient/id {:as :id})
                                  (:ingredient/name {:as :name})
                                  (:ingredient/info {:as :info})
                                  {(:id/level {:as :level})
                                   [(:level/id {:as :id})
                                    (:level/name {:as :name})]}
                                  {(:id/category {:as :category})
                                   [(:category/id {:as :id})
                                    (:category/name {:as :name})]}])]
       :in    [id]
       :where [[?ingredient :ingredient/name ?name]
               [(= ?ingredient id)]]}
     id))))

;; TODO implement this! using lucene?
(defn search [q]
  [])
#_(defn search
    "Search for ingredients with the given query string.
  This is case-insensitive and will search for ingredients where
  some part of the name, category, or level matches of the query string."
    [q]
    (let [query-string (str "%" q "%")
          category-ids {:select [:id]
                        :from   [:category]
                        :where  [:ilike :name query-string]}
          level-ids     {:select [:id]
                         :from   [:level]
                         :where  [:ilike :name query-string]}
          query        {:select [:id :name :info :category_id :level_id]
                        :from   [:ingredient]
                        :where  [:or
                                 [:ilike :name query-string]
                                 [:in :category_id category-ids]
                                 [:in :level_id level-ids]]}]
      (db/exec! query)))
