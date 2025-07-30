(ns card-engine.game.state.interface
  (:require
   [card-engine.game.state.core :as core]))

(defn make-game-state
  "Returns a new game state with default values.
  
  Args:
  * game-type: The type of game to create.
  * players: A list of player ids."
  [game-type players] (core/make-game-state game-type players))
