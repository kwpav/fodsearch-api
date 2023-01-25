(ns fodsearch-api.schema
  "Malli schemas for the basic representations of the data in the system.
  These are meant to be used in other ns's.")

(def Type
  [:enum "safe" "moderate" "unsafe"])

(def Category
  [:enum
   "vegetable"
   "fruit"
   "cereal/grain/nut/seed/flour"
   "meat/eggs/legumes/soy protein"
   "dairy and alternatives"
   "sweeteners/sauces/condiments"])

(def Ingredient
  [:map
   [:info string?]
   [:name string?]
   [:category Category]
   [:type Type]])
