(ns fodsearch.database.init
  (:require
   [fodsearch.database.interface :as db]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]))

(defn- create-tables!
  []
  (let [level-table
        "
CREATE TABLE IF NOT EXISTS level (
  id SERIAL PRIMARY KEY,
  name VARCHAR(10) UNIQUE NOT NULL
)"
        category-table
        "
CREATE TABLE IF NOT EXISTS category (
  id SERIAL PRIMARY KEY,
  name TEXT UNIQUE NOT NULL
)"
        ingredient-table
        "
CREATE TABLE IF NOT EXISTS ingredient (
  id SERIAL PRIMARY KEY,
  name TEXT UNIQUE NOT NULL,
  info TEXT,
  category_id INT NOT NULL,
  CONSTRAINT fk_category
    FOREIGN KEY(category_id)
    REFERENCES category(id),
  level_id INT NOT NULL,
  CONSTRAINT fk_level
    FOREIGN KEY(level_id)
    REFERENCES level(id)
)"]
    (jdbc/execute! db/conn [level-table])
    (jdbc/execute! db/conn [category-table])
    (jdbc/execute! db/conn [ingredient-table])))

(defn- read-ingredients-csv
  "All ingredient data is in `resources/database/ingredients.csv`.
  This reads the csv and puts it into a map so it can be
  easily inserted into the DB with HoneySQL."
  []
  (letfn [(csv-data->maps [csv-data]
            (mapv zipmap
                  (->> (first csv-data) ;; First row is the header
                       (map keyword)
                       repeat)
	              (rest csv-data)))]
    (with-open [reader (io/reader (io/resource "database/ingredients.csv"))]
      (csv-data->maps (csv/read-csv reader)))))

(defn- csv-column->sql
  "Helper function for levels and categories.
  Get all distinct values from csv data given a column.
  Return a vector with maps those values with the key of `:name`."
  [csv-data column]
  (->> csv-data
       (mapv #(assoc {} :name (get % column)))
       distinct
       (mapv vals)))

(defn- insert-levels-categories-data!
  "Insert the levels and categories from the csv into the DB.
  This needs to be done before inserting the ingredients
  because they are FKs on the `ingredient` table."
  []
  (let [data             (read-ingredients-csv)
        levels           (csv-column->sql data :level)
        categories       (csv-column->sql data :category)
        levels-query     {:insert-into [:level]
                          :columns     [:name]
                          :values      levels}
        categories-query {:insert-into [:category]
                          :columns     [:name]
                          :values      categories}
        queries          (map sql/format [levels-query categories-query])]
    (doseq [query queries]
      (jdbc/execute! db/conn query))))

(defn- ingredient-csv->sql
  "Helper function to turn ingredient csv data
  into a map so it can be inserted with HoneySQL.
  Levels and categories need to exist in the DB
  before this is run."
  [csv-data]
  (mapv (fn [{:keys [level category] :as ingredient}]
               (-> ingredient
                   (assoc :level_id
                          (:level/id
                           (first (db/exec!
                                   {:select [:level/id]
                                    :from   [:level]
                                    :where  [:= :level/name level]}))))
                   (assoc :category_id
                          (:category/id
                           (first (db/exec!
                                   {:select [:category/id]
                                    :from   [:category]
                                    :where  [:= :category/name category]}))))
                   (dissoc :category)
                   (dissoc :level)))
        csv-data))

(defn- insert-ingredients-data!
  "Insert the ingredient data from the csv.
  Needs to be run after levels and categories have been inserted."
  []
  (let [data        (read-ingredients-csv)
        ingredients (ingredient-csv->sql data)
        query       {:insert-into [:ingredient]
                     :values      ingredients}]
    (db/exec! query)))

(defn init-db!
  "Initialize the database.
  Creates tables and inserts data from `resources/ingredients.csv`"
  []
  (let [tables            (create-tables!)
        levels-categories (insert-levels-categories-data!)
        ingredients       (insert-ingredients-data!)]
    {:tables            tables
     :levels-categories levels-categories
     :ingredients       ingredients}))

(defn reset-db!
  "Drop all tables, recreate them, and insert data."
  []
    (jdbc/execute! db/conn ["
DROP TABLE IF EXISTS ingredient
"])
  (jdbc/execute! db/conn ["
DROP TABLE IF EXISTS category
"])
  (jdbc/execute! db/conn ["
DROP TABLE IF EXISTS level
"])
  (init-db!))

(comment
  (create-tables!)
  (insert-levels-categories-data!)

  (init-db!)
  (reset-db!)

  (jdbc/execute! db/conn ["SELECT * FROM level"])
  (jdbc/execute! db/conn ["SELECT * FROM category"])
  (jdbc/execute! db/conn ["SELECT * FROM ingredient"])
  ,)
