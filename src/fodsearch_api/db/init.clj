(ns fodsearch-api.db.init
  (:require
   [fodsearch-api.db.db :as db]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]))

(defn- create-tables!
  []
  (let [type-table
        "
CREATE TABLE IF NOT EXISTS type (
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
  type_id INT NOT NULL,
  CONSTRAINT fk_type
    FOREIGN KEY(type_id)
    REFERENCES type(id)
)"]
    (jdbc/execute! db/conn [type-table])
    (jdbc/execute! db/conn [category-table])
    (jdbc/execute! db/conn [ingredient-table])))

(defn- read-ingredients-csv
  "All ingredient data is in `resources/ingredients.csv`.
  This reads the csv and puts it into a map so it can be
  easily inserted into the DB with HoneySQL."
  []
  (letfn [(csv-data->maps [csv-data]
            (mapv zipmap
                  (->> (first csv-data) ;; First row is the header
                       (map keyword)
                       repeat)
	              (rest csv-data)))]
    (with-open [reader (io/reader (io/resource "ingredients.csv"))]
      (csv-data->maps (csv/read-csv reader)))))

(defn- csv-column->sql
  "Helper function for types and categories.
  Get all distinct values from csv data given a column.
  Return a vector with maps those values with the key of `:name`."
  [csv-data column]
  (->> csv-data
       (mapv #(assoc {} :name (get % column)))
       distinct
       (mapv vals)))

(defn- insert-types-categories-data!
  "Insert the types and categories from the csv into the DB.
  This needs to be done before inserting the ingredients
  because they are FKs on the `ingredient` table."
  []
  (let [data             (read-ingredients-csv)
        types            (csv-column->sql data :type)
        categories       (csv-column->sql data :category)
        types-query      {:insert-into [:type]
                          :columns     [:name]
                          :values      types}
        categories-query {:insert-into [:category]
                          :columns     [:name]
                          :values      categories}
        queries           (map sql/format [types-query categories-query])]
    (doseq [query queries]
      (jdbc/execute! db/conn query))))

(defn- ingredient-csv->sql
  "Helper function to turn ingredient csv data
  into a map so it can be inserted with HoneySQL.
  Types and categories need to exist in the DB
  before this is run."
  [csv-data]
  (mapv (fn [{:keys [type category] :as ingredient}]
               (-> ingredient
                   (assoc :type_id
                          (:type/id
                           (first (db/exec!
                                   {:select [:type/id]
                                    :from   [:type]
                                    :where  [:= :type/name type]}))))
                   (assoc :category_id
                          (:category/id
                           (first (db/exec!
                                   {:select [:category/id]
                                    :from   [:category]
                                    :where  [:= :category/name category]}))))
                   (dissoc :category)
                   (dissoc :type)))
        csv-data))

(defn- insert-ingredients-data!
  "Insert the ingredient data from the csv.
  Needs to be run after types and categories have been inserted."
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
  (let [tables           (create-tables!)
        types-categories (insert-types-categories-data!)
        ingredients      (insert-ingredients-data!)]
    {:tables           tables
     :types-categories types-categories
     :ingredients      ingredients}))

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
DROP TABLE IF EXISTS type
"])
  (init-db!))

(comment
  (create-tables!)
  (insert-types-categories-data!)
  (init-db!)
  (reset-db!)

  (jdbc/execute! db/conn ["SELECT * FROM type"])
  (jdbc/execute! db/conn ["SELECT * FROM category"])
  (jdbc/execute! db/conn ["SELECT * FROM ingredient"])
  ,)
