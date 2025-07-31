(ns card-engine.game.rules.interface
  (:require
   [card-engine.game.rules.core :as core]))

(defn load-rulset
  "Returns the ruleset with the given name.
  Throws an exception if the ruleset is not found.
  
  Args:
  * ruleset-name: The name of the ruleset to load."
  [ruleset-name]
  (core/load-ruleset ruleset-name))

(defn apply-rule
  "Applies the given rule to the game state if its condition is met.
  Returns the new game-state
  
  Args:
  * game-state: The current game state
  * rule: The rule to apply"
  [game-state rule]
  (core/apply-rule game-state rule))
