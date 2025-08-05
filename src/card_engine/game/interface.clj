;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.interface
  (:require
   [card-engine.game.core :as core]))

(defn make-game
  "Returns a new game with the given ruleset and players.
  
  Args:
  * ruleset-name: The name of the ruleset to use.
  * players: A sequence of player objects."
  [ruleset-name players] (core/make-game ruleset-name players))

(defn game-loop
  "Runs the game loop for the given game.
  
  Args:
  * game: The game object to run."
  [game] (core/game-loop game))
