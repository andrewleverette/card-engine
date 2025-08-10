;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.scoring
  "This namespace defines the functions that apply the scoring rules to the game state"
  (:require
   [card-engine.card.interface :as card]))

(defmulti score-hand
  "Calculates the score for a players hand based on the game type.

  Dispathers:
  :blackjack - Returns the face value of number cards, 10 for jack, queen, and king, and 11 for ace
  :highest-score - Returns the highest possible score
  :default - Returns 0"
  (fn [game-type _] game-type))

(defmethod score-hand :blackjack
  [_ hand]
  (let [card-ranks (map card/rank hand)
        ace-count (count (filter #{:ace} card-ranks))
        sum-minus-aces (reduce (fn [score rank]
                                 (cond
                                   (contains? #{:jack :queen :king} rank) (+ score 10)
                                   (number? rank) (+ score rank)
                                   :else score)) 0 (remove #{:ace} card-ranks))]
    (loop [total sum-minus-aces
           aces ace-count]
      (if (zero? aces)
        total
        (let [total-with-ace-high (+ total 11)]
          (if (> total-with-ace-high 21)
            (recur (inc total) (dec aces))
            (recur total-with-ace-high (dec aces))))))))

(defmethod score-hand :highest-score
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
  [game-type _]
  (throw (ex-info "Failed to score hand" {:type :score-hand
                                          :errors [{:type :unknown-game-type
                                                    :message "Unknown game type"
                                                    :value game-type}]})))
