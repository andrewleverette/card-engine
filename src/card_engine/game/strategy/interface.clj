;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.strategy.interface
  (:require
   [card-engine.game.strategy.core :as core]))

(defn get-player-action
  "Returns the player action for the given game state and player.
  Throws an exception if the game type or player strategy is unknown.

  Args:
  * game-state: The current game state
  * player: The player to get the action for"
  [game-state player] (core/get-player-action game-state player))
