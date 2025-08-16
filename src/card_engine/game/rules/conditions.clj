;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.conditions
  "This namespace defines the functions that check if a rule's condition is met. This
  determines if the rule should be applied to the game state or should be ignored.

  This is provided by a multimethod that dispatches on the rule's condition-type."
  (:require
   [card-engine.player.interface :as player]
   [card-engine.game.state.interface :as state]
   [card-engine.game.rules.comparisons :refer [comparison]]))

(defn- condition-type
  "Returns the condition type of the given rule."
  [rule]
  (get-in rule [:rule/condition :condition/type]))

(defn- condition-params
  "Returns the condition params of the given rule."
  [rule]
  (get-in rule [:rule/condition :condition/params]))

(defmulti check-condition
  "Checks if the given rule's condition is met.
  Returns true if the condition is met or no dispatcher is found.

  Args:
  * game-state: The current game state
  * rule: The rule to check

  Dispatchers:
  * :game-phase-matches - Checks if the game phase matches the given phase
  * :game-over-condition-met? - Checks if the game is over
  * :score-threshold - Checks if the player's score meets the given threshold"
  (fn [_ rule] (condition-type rule)))

(defmethod check-condition :game-phase-matches
  [game-state rule]
  (let [params (condition-params rule)]
    (= (state/phase game-state) (:phase params))))

(defmethod check-condition :player-status-matches
  [game-state rule]
  (let [params (condition-params rule)
        p (state/current-player game-state)]
    (= (player/status p) (:status params))))

(defmethod check-condition :player-status-in-set?
  [game-state rule]
  (let [params (condition-params rule)
        [_ p] (state/current-player game-state)]
    (contains? (:set params) (player/status p))))

(defmethod check-condition :all-players-status-in-set?
  [game-state rule]
  (let [params (condition-params rule)
        players (state/non-dealer-players game-state)]
    (every? #(contains? (:set params) (player/status %)) players)))

(defmethod check-condition :player-action-matches
  [game-state rule]
  (let [params (condition-params rule)
        [_ p] (state/current-player game-state)]
    (= (player/action p) (:action params))))

(defmethod check-condition :game-over-condition-met?
  [game-state _]
  (let [players (state/players game-state)]
    (every? #(#{:win :lose :tie} (player/status %)) players)))

(defmethod check-condition :score-threshold
  [game-state rule]
  (let [{:keys [target threshold operator]} (condition-params rule)
        ;; Currently only supports selecting the dealer or the current player
        ;; TODO: Add support for selecting a specific player
        [_ player] (cond
                     (= target :dealer) (state/dealer game-state)
                     (= target :current-player) (state/current-player game-state)
                     :else nil)]
    (when player
      (comparison operator (player/score player) threshold))))

(defmethod check-condition :default
  [_ _] true)
