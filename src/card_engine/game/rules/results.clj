;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.results
  "This namespace defines the functions that apply the results of rules to the game state."
  (:require
   [card-engine.player.interface :as player]
   [card-engine.game.state.interface :as state]))

(defn- make-results
  [player]
  {:results/player-id (player/id player)
   :results/player-name (player/player-name player)
   :results/hand (player/hand player)
   :results/score (player/score player)
   :results/status (player/status player)})

(defmulti calculate-results
  "Calculates the results of the given game state, based on the game type."
  (fn [game-state] (state/game-type game-state)))

(defmethod calculate-results :blackjack
  [game-state]
  (let [dealer (state/dealer game-state)
        dealer-score (player/score dealer)
        dealer-status (player/status dealer)
        players (state/players game-state)]
    (loop [players players
           results {:win []
                    :lose []
                    :tie []}]
      (if (empty? players)
        results
        (let [p (first players)
              p-score (player/score p)
              p-status (player/status p)]
          (cond
            (player/is-dealer? p) (recur (rest players) (assoc results :dealer (make-results p)))
            (= p-status :busted) (recur (rest players) (update results :lose conj (make-results p)))
            (= dealer-status :busted) (recur (rest players) (update results :win conj (make-results p)))
            (> p-score dealer-score) (recur (rest players) (update results :win conj (make-results p)))
            (< p-score dealer-score) (recur (rest players) (update results :lose conj (make-results p)))
            :else (recur (rest players) (update results :tie conj (make-results p)))))))))

(defmethod calculate-results :default
  [game-state]
  [[:game/handle-error {:type :calculate-results
                        :message "Failed to calculate results"
                        :errors [{:type :unknown-game-type
                                  :message "Unknown game type"
                                  :value (state/game-type game-state)}]}]])
