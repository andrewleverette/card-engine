;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.conditions
  "This namespace defines the functions that check if a rule's condition is met. This
  determines if the rule should be applied to the game state or should be ignored.

  This is provided by a multimethod that dispatches on the rule's condition-type."
  (:require
   [card-engine.player.interface :as player]
   [card-engine.game.state.interface :as state]))

(defmulti check-condition
  "Checks if the given rule's condition is met.
  Returns true if the condition is met or no dispatcher is found.

  Args:
  * game-state: The current game state
  * rule: The rule to check

  Dispatchers:
  * :game-phase-matches - Checks if the game phase matches the given phase
  * :game-over-condition-met? - Checks if the game is over"
  (fn [_ rule] (:rule/condition-type rule)))

(defmethod check-condition :game-phase-matches
  [game-state rule]
  (let [{:rule/keys [condition-params]} rule]
    (= (:game/phase game-state) (:phase condition-params))))

(defmethod check-condition :game-over-condition-met?
  [game-state _]
  (let [players (state/players game-state)]
    (every? #(#{:win :lose :tie} (player/status %)) players)))

(defmethod check-condition :default
  [_ _] true)
