(ns fodsearch.xtdb.domain)

(def IngredientDomain
  [:schema
   {:registry
    {::level      [:map
                   [:name string?]]
     ::category   [:map
                   [:name string?]]
     ::ingredient [:map
                   [:name string?]
                   [:category ::category]
                   [:level ::level]]}}])
