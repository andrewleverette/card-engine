(ns card-engine.card.spec
  (:require
   [clojure.spec.alpha :as s]))

(def suits #{:hearts :diamonds :clubs :spades})
(def ranks (into #{:ace :jack :queen :king} (range 2 11)))

(s/def :card/suit (s/and keyword? suits))
(s/def :card/rank ranks)
(s/def :card/card (s/keys :req [:card/rank :card/suit]))

(defn valid-suit?
  "Returns true if the given suit is a valid valid suit value.
  
  Args: suit - the suit to check"
  [suit]
  (s/valid? :card/suit suit))

(defn valid-rank?
  "Returns true if the given rank is a valid valid rank value."
  [rank]
  (s/valid? :card/rank rank))

(defn validate-card
  "Checks if the given rank and suit can form a valid card. If either the rank or
  the suit are not valid, returns a list of errors.
  
  Args:
  * rank - the rank of the card
  * suit - the suit of the card
  
  Returns a list of validation error objects or nil if the card is valid."
  [rank suit]
  (cond-> []
    (not (valid-rank? rank)) (conj {:type :invalid-rank
                                    :value rank
                                    :message (str "Rank '" rank "' is not valid.")
                                    :spec (s/explain-str :card/rank rank)})
    (not (valid-suit? suit)) (conj {:type :invalid-suit
                                    :value suit
                                    :message (str "Suit '" suit "' is not valid.")
                                    :spec (s/explain-str :card/suit suit)})
    :else seq))
