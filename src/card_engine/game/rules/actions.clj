(ns card-engine.game.rules.actions
  "This namespace defines the functions that apply the actions of rules to the game state and
  moves the game state forward.
  
  This is provided by a multimethod that dispatches on the rule's action-type."
  (:require
   [card-engine.player.interface :as player]
   [card-engine.deck.interface :as deck]
   [card-engine.game.state.interface :as state]
   [card-engine.game.rules.dealing :refer [deal-action]]))

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
  (fn [_ rule] (:rule/type rule)))

(defmethod apply-action :deal
  [game-state rule]
  (let [{:rule/keys [action-params]} rule]
    (deal-action game-state action-params)))

(defmethod apply-action :transition-game-status
  [game-state rule]
  (let [{:rule/keys [action-params]} rule]
    (state/set-status game-state (:status action-params))))

(defmethod apply-action :transition-phase
  [game-state rule]
  (let [{:rule/keys [action-params]} rule]
    (state/set-phase game-state (:next-phase action-params))))

(defmethod apply-action :transition-player
  [game-state rule]
  (let [{:rule/keys [_]} rule
        [p-idx _] (state/current-player game-state)
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

(defmethod apply-action :card-management
  [game-state rule]
  (let [{:rule/keys [action-params]} rule]
    (if (= :collect-all-cards (:action action-params))
      (let [players (mapv #(player/reset-player %) (state/players game-state))
            new-deck (deck/shuffle-deck (deck/make-deck))]
        (-> game-state
            (assoc-in [:game/players] players)
            (state/set-deck-state {:deck/draw-pile new-deck
                                   :deck/discard-pile []})
            (state/set-table-state {})
            (state/set-current-player nil)))
      game-state)))

(defmethod apply-action :default
  [game-state _] game-state)
