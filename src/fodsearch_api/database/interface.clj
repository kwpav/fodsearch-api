(ns fodsearch-api.database.interface
  (:require
   [xtdb.api :as xt]
   [fodsearch-api.database.init :as init]))

(defonce node2
  (xt/start-node {}))

(defn query
  [node & args]
  (tap> {::query node})
  (apply xt/q (xt/db node) args))

(defn pull
  [node & args]
  (apply xt/pull (xt/db node) args))

(defn sync
  [node & args]
  (apply xt/sync node args))

(defn init-db
  [node]
  (let [ingredients (init/csv->ingredients (init/read-ingredients-csv))]
    (init/put-all node ingredients)
    (xt/sync node)))

(comment
  (xt/sync node)

  (query
   '{:find  [?id ?name]
     :keys  [id name]
     :where [[?id :level/name ?name]]})

  (xt/q (xt/db node)
        '{:find  [?id ?name]
          :keys  [id name]
          :where [[?id :category/name ?name]]})

  (xt/q (xt/db node)
        '{:find  [?name]
          :where [[?id :level/name "safe"]
                  [e :id/level ?id]
                  [e :ingredient/name ?name]]})

  (xt/q (xt/db node)
        '{:find  [?name]
          :in    [level-name]
          :where [[?id :level/name level-name]
                  [e :id/level ?id]
                  [e :ingredient/name ?name]]}
        "safe"
        #_"moderate")

  ;; get all ingredients - flat
  (xt/q (xt/db node)
        '{:find  [?id ?name ?info ?level ?category]
          :where [[?id :ingredient/name ?name]
                  [?id :ingredient/info ?info]
                  [?id :id/level ?level-id]
                  [?id :id/category ?category-id]
                  [?level-id :level/name ?level]
                  [?category-id :category/name ?category]]})

  (mapv first
        (xt/q
         (xt/db node)
         '{:find  [(pull ?ingredient [(:xt/id {:as :id})
                                      (:ingredient/name {:as :name})
                                      (:ingredient/info {:as :info})
                                      {(:id/level {:as :level})
                                       [(:xt/id {:as :id})
                                        (:level/name {:as :name})]}
                                      {(:id/category {:as :category})
                                       [(:xt/id {:as :id})
                                        (:category/name {:as :name})]}])]
           :where [[?ingredient :ingredient/name ?name]]}))

  ;; ?????
  (xt/q (xt/db node)
        '{:find  [?id ?name ?info ?category ?level]
          :keys  [id name info category level]
          :where [[?level-id :level/name "moderate"]
                  [e :id/level ?level-id]
                  [e :id/category ?category-id]
                  [?id :ingredient/name ?name]
                  [e :ingredient/info ?info]
                  [?category-id :category/name ?category]
                  [?level-id :level/name ?level]]})

  (xt/q (xt/db node)
        '{:find  [?name]
          :where [[id :level/name "safe"]
                  [e :id/level]
                  [e :ingredient/name ?name]]})

  (def manifest
    {:xt/id       :manifest
     :pilot-name  "Johanna"
     :id/rocket   "SB002-sol"
     :id/employee "22910x2"
     :badges      "SETUP"
     :cargo       ["stereo" "gold fish" "slippers" "secret note"]})

  (def manifest
    {:xt/id       :manifest
     :pilot-name  "Johanna"
     :id/rocket   "SB002-sol"
     :id/employee "22910x2"
     :badges      "SETUP"
     :cargo       ["stereo" "gold fish" "slippers" "secret note"]})

  (xt/submit-tx node [[::xt/put manifest]])

  (xt/sync node)

  (xt/entity (xt/db node) :manifest)

  :comment)
