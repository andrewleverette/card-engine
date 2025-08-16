;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.dealing
  "This namespace defines the functions that deal cards to players.
  
  This is provided by a multimethod that dispatches on the rule's action params
  target value."
  (:require
   [card-engine.deck.interface :as deck]
   [card-engine.player.interface :as player]
   [card-engine.game.state.interface :as state]))

(defn deal-to-player
  "Deals the given number of cards to the given player.
  
  Args
  * draw-pile - The draw pile to deal from
  * p - The player to deal to
  * from - The source of the draw pile
  * num-cards - The number of cards to deal

  Returns: 
  * A vector of 3 actions that modify the game state
    - Update the remaining draw pile
    - Update the draw pile status, ie. :deck-empty
    - Update the player"
  [draw-pile p from num-cards]
  (let [{:keys [dealt remaining status]} (deck/deal-cards draw-pile num-cards)
        p' (player/add-cards p dealt)]
    [[:state/assoc-in
      [:game/deck-state from] remaining]
     [:state/assoc-in
      [:game/deck-state :deck/status] status]
     [:state/assoc-in
      [:game/players (player/id p)] p']]))

(defn deal-to-many-players
  "Deals the given number of cards to the given players.
  
  Args:
  * draw-pile - The draw pile to deal from
  * players - The players to deal to
  * from - The source of the draw pile
  * num-cards - The number of cards to deal
  
  Returns:
  * A vector of actions that modify the game state. There will be 3
    actions for each player.
    - Update the remaining draw pile
    - Update the draw pile status, ie. :deck-empty
    - Update the player"
  [draw-pile players from num-cards]
  (first (reduce (fn [[actions remaining] p]
                   (let [{:keys [dealt remaining status]} (deck/deal-cards remaining num-cards)
                         p' (player/add-cards p dealt)]
                     [(conj actions
                            [:state/assoc-in [:game/players (player/id p)] p']
                            [:state/assoc-in [:game/deck-state from] remaining]
                            [:state/assoc-in [:game/deck-state :deck/status] status])
                      remaining]))
                 [[] draw-pile]
                 players)))

(defmulti deal-action
  "Applies the deal action to the game state and 
  returns the corresponding actions to be be applied to the current state.

  If no dispatcher is found, returns an error action.
  
  Args:
  * game-state: The current game state
  * params: The rule's action params
  
  Dispatchers:
  * :current-player - Deals the given number of cards to the current player
  * :all-players - Deals the given number of cards to all players
  * :all-non-dealers - Deals the given number of cards to all non-dealers
  * :dealer - Deals the given number of cards to the dealer"
  (fn [_ params] (:target params)))

(defmethod deal-action :current-player
  [game-state params]
  (let [{:keys [num-cards from]} params
        draw-pile (get-in game-state [:game/deck-state from])
        player (state/current-player game-state)]
    (if player
      (deal-to-player draw-pile player from num-cards)
      [[:game/handle-error {:type :apply-deal-action
                            :message "Failed to apply deal action"
                            :params params
                            :errors [{:type :no-current-player
                                      :message "No current player"}]}]])))

(defmethod deal-action :all-players
  [game-state params]
  (let [{:keys [num-cards from]} params
        draw-pile (get-in game-state [:game/deck-state from])
        players (map #(state/player game-state (player/id %)) (state/players game-state))]
    (deal-to-many-players draw-pile players from num-cards)))

(defmethod deal-action :all-non-dealers
  [game-state params]
  (let [{:keys [num-cards from]} params
        draw-pile (get-in game-state [:game/deck-state from])
        players (state/non-dealer-players game-state)]
    (deal-to-many-players draw-pile players from num-cards)))

(defmethod deal-action :dealer
  [game-state params]
  (let [{:keys [num-cards from]} params
        draw-pile (get-in game-state [:game/deck-state from])
        dealer (state/dealer game-state)]
    (if dealer
      (deal-to-player draw-pile dealer from num-cards)
      [[:game/handle-error {:type :apply-deal-action
                            :message "Failed to apply deal action"
                            :params params
                            :errors [{:type :no-dealer
                                      :message "No dealer"}]}]])))

(defmethod deal-action :default
  [_ params]
  [[:game/handle-error {:type :apply-deal-action
                        :message "Failed to apply deal action"
                        :params params
                        :errors [{:type :unknown-deal-action
                                  :message "Unknown deal action target"
                                  :value (:target params)}]}]])
