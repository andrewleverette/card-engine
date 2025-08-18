;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.actions
  "This namespace defines the functions that apply the actions of rules to the game state and
  returns the modifications that should be applied to the game state.
  
  This is provided by a multimethod that dispatches on the rule's action-type."
  (:require
   [card-engine.player.interface :as player]
   [card-engine.deck.interface :as deck]
   [card-engine.game.state.interface :as state]
   [card-engine.game.strategy.interface :as strategy]
   [card-engine.game.rules.dealing :refer [deal-action]]
   [card-engine.game.rules.results :refer [calculate-results]]
   [card-engine.game.rules.scoring :refer [score-hand]]))

(defn action-type
  "Returns the action type of the given rule."
  [rule]
  (get-in rule [:rule/action :action/type]))

(defn action-params
  "Returns the action params of the given rule."
  [rule]
  (get-in rule [:rule/action :action/params]))

(defmulti apply-action
  "Applies the given action to the game state and
  returns the corresponding actions to be be applied to the current state. 
  
  Args:
  * game-state: The current game state
  * rule: The rule to apply
  
  Dispatchers:
  * :deal - Deals the given number of cards to the given target from the given source.
  * :transition-status - Sets the game status to the given status.
  * :transition-phase - Sets the game phase to the given phase.
  * :transition-player - Sets the current player to the next player.
  * :transition-dealer - Sets the current player to the dealer.
  * :get-player-action - Gets the player action for the current player.
  * :update-player-status - Updates the player status to the given status.
  * :reset-game - Resets the deck state and player state
  * :score-player-hand - Scores the current player's hand and updates the player's score.
  * :score-dealer-hand - Scores the dealer's hand and updates the dealer's score.
  * :calculate-results - Calculates the game results and updates the game state."
  (fn [_ rule] (action-type rule)))

(defmethod apply-action :deal
  [game-state rule]
  (let [params (action-params rule)]
    (deal-action game-state params)))

(defmethod apply-action :transition-status
  [_ rule]
  (let [params (action-params rule)]
    [[:state/assoc :game/status (:status params)]]))

(defmethod apply-action :transition-phase
  [_ rule]
  (let [params (action-params rule)]
    [[:state/assoc :game/phase (:phase params)]]))

;; TODO: This needs some work. I need to figure out a better
;; way to define turn order, but this will work for now.
(defmethod apply-action :transition-player
  [game-state _]
  (let [p (state/current-player game-state)
        players (state/players game-state)
        p-idx (->> players
                   (map-indexed (fn [idx p] [idx (player/id p)]))
                   (filter (fn [[_ p-id]] (= p-id (player/id p))))
                   ffirst)
        next-player-idx (if (nil? p-idx) 0 (mod (inc p-idx) (count players)))
        next-player (get players next-player-idx)]
    (if (player/is-dealer? next-player)
        ;; If the next player is a dealer, 
        ;; set the current player to the next non-dealer 
      (let [idx (mod (inc next-player-idx) (count players))
            player (get players idx)]
        [[:state/assoc :game/current-player-id (player/id player)]])
      [[:state/assoc :game/current-player-id (player/id next-player)]])))

(defmethod apply-action :transition-dealer
  [game-state _]
  (let [dealer (state/dealer game-state)]
    [[:state/assoc :game/current-player-id (player/id dealer)]]))

(defmethod apply-action :get-player-action
  [game-state _]
  (let [p (state/current-player game-state)]
    (strategy/get-player-action game-state p)))

(defmethod apply-action :update-player-status
  [game-state rule]
  (let [params (action-params rule)
        p (state/current-player game-state)
        p' (player/set-status p (:status params))]
    [[:state/assoc-in [:game/players (player/id p)] p']]))

;; TODO: I'm not sure about this one either. Shuffling the deck
;; is definitely a mutation, but there needs to be a way to reset
;; the game state to a clean slate. This needs to be testable though.
;; I'll leave this untested for now.
(defmethod apply-action :reset-game
  [game-state _]
  (let [player-map (into {} (mapv
                             (fn [p] [(player/id p) (player/reset-player p)])
                             (state/players game-state)))
        new-deck (deck/shuffle-deck (deck/make-deck))]
    [[:state/assoc
      :game/players player-map
      :game/deck-state {:deck/draw-pile new-deck
                        :deck/discard-pile []}
      :game/table-state {}
      :game/current-player-id nil]]))

(defmethod apply-action :score-player-hand
  [game-state _]
  (let [p (state/current-player game-state)
        game-type (state/game-type game-state)
        hand (player/hand p)
        score-result (score-hand game-type hand)]
    (if (number? score-result)
      [[:state/assoc-in [:game/players (player/id p)] (player/set-score p score-result)]]
      score-result)))

(defmethod apply-action :score-dealer-hand
  [game-state _]
  (let [d (state/dealer game-state)
        game-type (state/game-type game-state)
        hand (player/hand d)
        d' (player/set-score d (score-hand game-type hand))]
    [[:state/assoc-in [:game/players (player/id d)] d']]))

(defmethod apply-action :calculate-results
  [game-state _]
  (let [results (calculate-results game-state)]
    (if (= :game/handle-error (ffirst results))
      results
      [[:state/assoc :game/results results]])))

(defmethod apply-action :default
  [_ rule]
  [[:game/handle-error {:type :apply-action
                        :message "Failed to apply action"
                        :errors [{:type :unknown-action-type
                                  :message "Unknown action type"
                                  :value (action-type rule)}]}]])
