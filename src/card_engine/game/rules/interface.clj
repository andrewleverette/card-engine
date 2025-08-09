;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.interface
  "Provides a public interface to the rules engine."
  (:require
   [card-engine.game.rules.core :as core]))

(defn load-rulset
  "Returns the ruleset with the given name.
  Throws an exception if the ruleset is not found.
  
  Args:
  * ruleset-name: The name of the ruleset to load."
  [ruleset-name]
  (core/load-ruleset ruleset-name))

(defn list-rulesets
  "Returns a list of all available rulesets defined in resources/game_rules."
  [] (core/list-rulesets))

(defn rule-type
  "Returns the type of the given rule.
  
  Args:
  * rule: The rule object"
  [rule] (core/rule-type rule))

(defn condition
  "Returns the condition of the given rule.

  Args:
  * rule: The rule object"
  [rule] (core/condition rule))

(defn action
  "Returns the action of the given rule.
  
  Args:
  * rule: The rule object"
  [rule] (core/action rule))

(defn apply-rule
  "Applies the given rule to the game state if its condition is met.
  Returns the new game-state
  
  Args:
  * game-state: The current game state
  * rule: The rule to apply"
  [game-state rule]
  (core/apply-rule game-state rule))

(defn apply-ruleset
  "Applies the rules in the given ruleset to the game state.
  Returns the new game-state or throws an exception if the ruleset cannot
  be applied.
  
  Args:
  * game-state: The current game state
  * ruleset: The ruleset to apply"
  [game-state ruleset]
  (core/apply-ruleset game-state ruleset))
