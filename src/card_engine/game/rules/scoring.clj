(ns card-engine.game.rules.scoring
  "This namespace defines the functions that apply the scoring rules to the game state"
  (:require
   [card-engine.card.interface :as card]))

(defmulti score-hand
  "Calculates the score for a players hand based on the game type.

  Dispathers:
  :standard - Returns the face value of number cards, 10 for jack, queen, and king, and 11 for ace
  :default - Returns 0"
  (fn [game-type hand] game-type))

(defmethod score-hand :highest-card
  [_ hand]
  (reduce (fn [score card]
            (let [rank (card/rank card)]
              (cond
                (number? rank) (+ score rank)
                (= :jack rank) (+ score 11)
                (= :queen rank) (+ score 12)
                (= :king rank) (+ score 13)
                (= :ace rank) (+ score 14)
                :else score))) 0 hand))

(defmethod score-hand :default
  [_ hand] 0)
