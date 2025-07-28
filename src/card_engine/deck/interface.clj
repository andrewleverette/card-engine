(ns card-engine.deck.interface
  "Provides an interface for working with card decks."
  (:require
   [card-engine.deck.core :as core]))

(defn make-deck
  "Returns a standard 52 card deck with a default ordering of cards."
  [] (core/make-deck))

(defn cards
  "Returns the cards in the deck."
  [deck] (core/cards deck))

(defn shuffle-deck
  "Given a deck, returns the deck with its cards shuffled.
  
  Args:
  * deck - the deck to shuffle"
  [deck] (core/shuffle-deck deck))

(defn deal-card
  "Deals the top card from the deck.
  
  Args: 
  * deck - the deck to deal from
  
  Retruns a map containing the dealt card and the remaining deck. 
  If the deck is empty, the dealt card is nil a status of :deck-empty
  is included in the map."
  [deck] (core/deal-card deck))

(defn deal-cards
  "Deals the top n cards from the deck.
  
  Args:
  * deck - the deck to deal from
  * n - the number of cards to deal
  
  Returns a map containing the dealt cards and the remaining deck.
  If the deck is empty, the dealt cards are an empty vector.
  If the deck has less than n cards, the maximum number of cards are dealt,
  the remaining deck is empty, and the status of :deck-empty is included
  in the map."
  [deck n] (core/deal-cards deck n))
