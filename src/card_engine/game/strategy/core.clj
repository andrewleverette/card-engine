;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.strategy.core
  (:require
   [clojure.string :as str]
   [card-engine.card.interface :as card]
   [card-engine.player.interface :as player]
   [card-engine.game.state.interface :as state]))

(defmulti get-player-action
  "Returns the data that describe how to get the player action.
  This could be an IO event that has a corresponding state change or 
  a direct game state change.

  Returns error dat the game type or player strategy is unknown.

  Dispatches based on tuple of game type and player strategy.
  
  Args:
  * game-state: The current game state
  * player: The player to get the action for"
  (fn [game-state player]
    [(state/game-type game-state) (player/strategy player)]))

(defmethod get-player-action [:blackjack :interactive]
  [game-state player]
  (let [dealer (state/dealer game-state)]
    [[:game/handle-io {:prompt/player-id (player/id player)
                       :prompt/message (str "Player: "
                                            (player/->short-str player)
                                            "\n" "Current hand: "
                                            (str/join ", " (map card/->str (player/hand player)))
                                            "\n" "Dealers face up card: "
                                            (card/->str (first (player/hand dealer))))
                       :prompt/actions [:hit :stand]
                       :prompt/output-key :player/action}]]))

(defmethod get-player-action :default
  [game-state player]
  [[:game/handle-error {:type :get-player-action
                        :message "Failed to get player action"
                        :errors [{:type :unknown-game-type
                                  :message "Unknown game type"
                                  :value {:game-type (state/game-type game-state)
                                          :player-strategy (player/strategy player)}}]}]])
