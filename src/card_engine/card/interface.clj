;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.card.interface
  "Provides a public interface for working with cards."
  (:require
   [card-engine.card.core :as core]))

;; --- Public Interface ---

;; --- Constructors ---
(defn make-card
  "Returns a card with the given rank and suit.
  
  Args:
  * rank - the rank of the card
  * suit - the suit of the card
  
  Returns a card object or throws an exception if the card is invalid."
  [rank suit]
  (core/make-card rank suit))

;; --- Selectors ---

(defn rank
  "Returns the rank of the card.
  
  Args:
  * card - the card to get the rank of"
  [card] (core/rank card))

(defn suit
  "Returns the suit of the card.
  
  Args:
  * card - the card to get the suit of"
  [card] (core/suit card))

;; --- Comparators ---

(defn suit-comparator
  "Returns the default comparison of the suits of two cards."
  [suit1 suit2] (core/default-suit-comaparator suit1 suit2))

(defn rank-comparator
  "Returns the default comparison of the ranks of two cards."
  [rank1 rank2] (core/default-rank-comparator rank1 rank2))

(defn card-comparator
  "Returns the default comparison of two cards."
  [card1 card2] (core/default-card-comparator card1 card2))

(defn sort-cards
  "Sorts a collection of cards using a given comparator function. If no comparator
  function is given, the default comparator is used.
  
  Args:
  * cards - the collection of cards to sort
  * comparator-fn - the comparator function to use to sort the cards
  
  Returns a sorted collection of cards."
  ([cards] (core/sort-cards cards))
  ([cards comparator-fn] (core/sort-cards cards comparator-fn)))

;; --- String Representation ---

(defn ->str
  "Returns a string representation of the card."
  [card] (core/->str card))
