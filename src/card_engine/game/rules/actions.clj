;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.actions
  "This namespace defines the functions that apply the actions of rules to the game state and
  moves the game state forward.
  
  This is provided by a multimethod that dispatches on the rule's action-type."
  (:require
   [card-engine.player.interface :as player]
   [card-engine.deck.interface :as deck]
   [card-engine.game.state.interface :as state]
   [card-engine.game.strategy.interface :as strategy]
   [card-engine.game.rules.dealing :refer [deal-action]]
   [card-engine.game.rules.scoring :refer [score-hand]]))

(defn- action-type
  "Returns the action type of the given rule."
  [rule]
  (get-in rule [:rule/action :action/type]))

(defn- action-params
  "Returns the action params of the given rule."
  [rule]
  (get-in rule [:rule/action :action/params]))

(defmulti apply-action
  "Applies the given action to the game state and
  returns the new game-state. If no dispatcher is found, 
  returns the game-state unchanged.
  
  Args:
  * game-state: The current game state
  * rule: The rule to apply
  
  Dispatchers:
  * :deal - Deals the given number of cards to the given target from the given source.
  * :transition-game-status - Sets the game status to the given status.
  * :transition-phase - Sets the game phase to the given phase.
  * :transition-player - Sets the current player to the next player.
  * :card-management - Resets the deck state and player state"
  (fn [_ rule] (action-type rule)))

(defmethod apply-action :deal
  [game-state rule]
  (let [params (action-params rule)]
    (deal-action game-state params)))

(defmethod apply-action :transition-game-status
  [game-state rule]
  (let [params (action-params rule)]
    (state/set-status game-state (:status params))))

(defmethod apply-action :transition-phase
  [game-state rule]
  (let [params (action-params rule)]
    (state/set-phase game-state (:next-phase params))))

(defmethod apply-action :transition-player
  [game-state _]
  (let [[p-idx _] (state/current-player game-state)
        players (state/players game-state)
        next-player-idx (if (nil? p-idx) 0 (mod (inc p-idx) (count players)))
        next-player (get players next-player-idx)]
    (if (player/is-dealer? next-player)
        ;; If the next player is a dealer, 
        ;; set the current player to the next non-dealer 
      (let [idx (mod (inc next-player-idx) (count players))
            player (get players idx)]
        (state/set-current-player game-state (player/id player)))
      (state/set-current-player game-state (player/id next-player)))))

(defmethod apply-action :get-player-action
  [game-state _]
  (let [[p-idx p] (state/current-player game-state)
        action (strategy/get-player-action game-state p)
        p' (player/set-action p action)]
    (assoc-in game-state [:game/players p-idx] p')))

(defmethod apply-action :update-player-status
  [game-state rule]
  (let [params (action-params rule)
        [p-idx p] (state/current-player game-state)
        p' (player/set-status p (:status params))]
    (assoc-in game-state [:game/players p-idx] p')))

(defmethod apply-action :card-management
  [game-state rule]
  (let [params (action-params rule)]
    (if (= :collect-all-cards (:action params))
      (let [players (mapv #(player/reset-player %) (state/players game-state))
            new-deck (deck/shuffle-deck (deck/make-deck))]
        (-> game-state
            (assoc-in [:game/players] players)
            (state/set-deck-state {:deck/draw-pile new-deck
                                   :deck/discard-pile []})
            (state/set-table-state {})
            (state/set-current-player nil)))
      game-state)))

(defmethod apply-action :score-player-hand
  [game-state _]
  (let [[p-idx p] (state/current-player game-state)
        game-type (state/game-type game-state)
        hand (player/hand p)
        p' (player/set-score p (score-hand game-type hand))]
    (assoc-in game-state [:game/players p-idx] p')))

(defmethod apply-action :score-all-player-hands
  [game-state _]
  (let [game-type (state/game-type game-state)
        players (state/players game-state)
        scored-players (mapv (fn [p]
                               (let [hand (player/hand p)
                                     score (score-hand game-type hand)]
                                 (player/set-score p score)))
                             players)]
    (assoc game-state :game/players scored-players)))

(defmethod apply-action :evaluate-players-vs-dealer-result
  [game-state _]
  (let [[_ dealer] (state/dealer game-state)
        dealer-score (player/score dealer)
        all-players (state/players game-state)
        updated-players (mapv (fn [p]
                                (cond
                                  (player/is-dealer? p) p
                                  (> (player/score p) dealer-score) (player/set-status p :won)
                                  (< (player/score p) dealer-score) (player/set-status p :lost)
                                  :else (player/set-status p :tie)))
                              all-players)]
    (assoc game-state :game/players updated-players)))

(defmethod apply-action :evaluate-player-vs-player-result
  [game-state _]
  (let [players (state/players game-state)
        scores (mapv player/score players)
        max-score (if (seq scores) (apply max scores) 0)
        updated-players (mapv (fn [p]
                                (if (= (player/score p) max-score)
                                  (player/set-status p :won)
                                  (player/set-status p :lost)))
                              players)]
    (assoc game-state :game/players updated-players)))

(defmethod apply-action :default
  [game-state rule]
  (throw (ex-info "Failed to apply action" {:type :apply-action
                                            :errors [{:type :unknown-action-type
                                                      :message "Unknown action type"
                                                      :value (action-type rule)}]})))
