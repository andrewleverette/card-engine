(ns card-engine.card.interface
  (:require
   [card-engine.card.core :as core]))

(defn make-card
  "Returns a card with the given rank and suit."
  [rank suit]
  (core/make-card rank suit))

(defn rank
  "Returns the rank of the card."
  [card] (core/rank card))

(defn suit
  "Returns the suit of the card."
  [card] (core/suit card))

(defn ->str
  "Returns a string representation of the card."
  [card] (core/->str card))
