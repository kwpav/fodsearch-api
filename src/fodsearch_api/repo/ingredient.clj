(ns fodsearch-api.repo.ingredient
  (:require
   [fodsearch-api.db.db :as db]))

(defn select-all
  "Select all ingredients."
  []
  (mapv first
        (db/query
         '{:find  [(pull ?ingredient [(:xt/id {:as :id})
                                      (:ingredient/name {:as :name})
                                      (:ingredient/info {:as :info})
                                      {(:id/level {:as :level})
                                       [(:xt/id {:as :id})
                                        (:level/name {:as :name})]}
                                      {(:id/category {:as :category})
                                       [(:xt/id {:as :id})
                                        (:category/name {:as :name})]}])]
           :where [[?ingredient :ingredient/name ?name]]})))

;; TODO allow other values besides ID - or separate functions?
(defn select
  "Select ingredient(s) where `by = value`."
  [by value]
  (first
   (first
    (db/query
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
       :where [[?ingredient :ingredient/id id]]}
     value))))

;; TODO implement this! using lucene?
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
