(ns card-engine.card.core
  (:require
   [clojure.string :as str]
   [card-engine.card.spec :as spec]))

;; --- Private Utility Functions

(defn- index-of
  [coll value]
  (.indexOf coll value))

(defn- index-of-suit
  [suit]
  (index-of spec/default-suits-order suit))

(defn- index-of-rank
  [rank]
  (index-of spec/default-ranks-order rank))

(defn- rank->str
  [rank]
  (if (keyword? rank)
    (str/capitalize (name rank))
    (str rank)))

(defn- suit->str
  [suit]
  (str/capitalize (name suit)))

;; --- Constructors ---

(defn make-card
  [rank suit]
  (if-let [errors (spec/validate-card rank suit)]
    (throw (ex-info "Invalid card form" {:type :make-card
                                         :errors errors}))
    {:card/rank rank :card/suit suit}))

;; --- Selectors ---

(defn rank [card]
  (:card/rank card))

(defn suit [card]
  (:card/suit card))

;; --- Comparators and Sorting ---

(defn default-suit-comaparator
  "Compares two suits using the default suits order (Alphabetical)."
  [suit1 suit2]
  (let [s1 (index-of-suit suit1)
        s2 (index-of-suit suit2)]
    (compare s1 s2)))

(defn default-rank-comparator
  "Compares two ranks using the default ranks order Ace -> King."
  [rank1 rank2]
  (let [r1 (index-of-rank rank1)
        r2 (index-of-rank rank2)]
    (compare r1 r2)))

(defn default-card-comparator
  "Compares two cards using the default suit and rank order."
  [card1 card2]
  (let [suit-comparison (default-suit-comaparator (suit card1) (suit card2))]
    (if (zero? suit-comparison)
      (default-rank-comparator (rank card1) (rank card2))
      suit-comparison)))

(defn sort-cards
  ([cards] (sort-cards cards default-card-comparator))
  ([cards comparator-fn] (sort comparator-fn cards)))

;; --- String Representation ---

(defn ->str
  [card]
  (let [r (rank card)
        s (suit card)]
    (str (rank->str r) " of " (suit->str s))))
