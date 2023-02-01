(ns fodsearch-api.db.db
  (:require
   [honey.sql :as sql]
   [next.jdbc :as jdbc]))

;; run psql with cmd:
;; docker run -it --rm --network fodsearch-api_default --link fodsearch-api-db-1:postgres postgres psql -h postgres -U postgres

(def db {:dbtype   "postgresql"
         :dbname   "testdb"
         :user     "postgres"
         :password "unsafe"
         :port     3002})

(def conn (jdbc/get-datasource db))

(defn exec!
  "Execute a HoneySQL query."
  [query]
  (->> query
       sql/format
       (jdbc/execute! conn)))
