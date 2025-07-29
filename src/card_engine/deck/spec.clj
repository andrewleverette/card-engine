(ns card-engine.deck.spec
  (:require
   [clojure.spec.alpha :as s]))

(s/def :deck/cards (s/coll-of :card/card))
(s/def :deck/deck (s/keys :req [:deck/cards]))

(defn valid-card-coll?
  "Returns true if the collection of cards is valid.
  
  Args:
  * cards - the collection of cards to check"
  [cards]
  (s/valid? :deck/cards cards))

(defn validate-deck
  "Checks if a collection of cards is valid and can form a valid deck. If the 
  collection of cards is not valid, returns a list of errors.
  
  Args:
  * cards - the collection of cards to check
  
  Returns a list of validation of error objects or nil if the deck is valid."
  [cards]
  (cond-> []
    (not (valid-card-coll? cards)) (conj {:type :invalid-card-coll
                                          :value cards
                                          :message "Invalid card collection."
                                          :spec (s/explain-str :deck/cards cards)})
    :else seq))
