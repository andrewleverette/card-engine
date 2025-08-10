;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.strategy.core
  (:require
   [clojure.string :as str]
   [card-engine.card.interface :as card]
   [card-engine.player.interface :as player]
   [card-engine.game.state.interface :as state]))

(defmulti get-player-action
  "Returns the player action for the given game state and player.
  Throws an exception if the game type or player strategy is unknown.
  Dispatches based on tuple of game type and player strategy.
  
  Args:
  * game-state: The current game state
  * player: The player to get the action for
  
  Dispatchers:"
  (fn [game-state player]
    [(state/game-type game-state) (player/strategy player)]))

(defmethod get-player-action [:blackjack :interactive]
  [game-state player]
  (let [[_ dealer] (state/dealer game-state)]
    (println "It's player " (player/->short-str player) "'s turn")
    (println "Current hand: ")
    (println (str/join "\n" (map card/->str (player/hand player))))
    (println "Dealers face up card: " (card/->str (first (player/hand dealer))))
    (println "Available actions: :hit or :stand")
    (print "> ")
    (flush)
    (read-string (read-line))))

(defmethod get-player-action :default
  [game-state player]
  (throw (ex-info "Unable to get player action" {:type :get-player-action
                                                 :value {:game-type (state/game-type game-state)
                                                         :player-strategy (player/strategy player)}
                                                 :message "Unknown game type or player strategy"})))
