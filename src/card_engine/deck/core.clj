(ns card-engine.deck.core
  (:require
   [card-engine.card.interface :as card]
   [card-engine.card.spec :as card-spec]))

(defn make-deck
  "Returns a new deck of cards."
  ([]
   (let [cards (for [suit card-spec/default-suits-order
                     rank card-spec/default-ranks-order]
                 (card/make-card rank suit))]
     (make-deck cards)))
  ([cards]
   {:deck/cards cards}))

(defn cards
  "Returns the cards in the deck."
  [deck]
  (:deck/cards deck))

(defn shuffle-deck
  "Returns a shuffled deck of cards."
  [deck]
  (update deck :deck/cards shuffle))

(defn deal-card
  "Deals the top card from the deck."
  [deck]
  (let [cards (cards deck)]
    (if (empty? cards)
      {:dealt nil :remaining deck :status :deck-empty}
      {:dealt (peek cards) :remaining (make-deck (pop cards))})))

(defn deal-cards
  "Deals n cards from the deck."
  [deck n]
  (loop [cards []
         deck' deck
         x n]
    (if (zero? x)
      {:dealt cards :remaining deck'}
      (let [{:keys [dealt remaining status]} (deal-card deck')]
        (if (= status :deck-empty)
          {:dealt cards :remaining remaining :status :deck-empty}
          (recur (conj cards dealt) remaining (dec x)))))))
