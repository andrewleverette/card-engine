;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.core
  (:require
   [card-engine.player.interface :as player]
   [card-engine.game.rules.interface :as rules]
   [card-engine.game.state.interface :as state]))

(defn make-game
  [ruleset-name players]
  (let [ruleset (rules/load-rulset ruleset-name)
        game-type (:game/type ruleset)
        game-state (state/make-game-state game-type players)]
    {:game/state game-state
     :game/ruleset ruleset}))

(defn game-winners
  [game-state]
  (->> game-state
       state/players
       (filter #(= :win (player/status %)))
       (map player/->short-str)))

(defn game-loop
  [game]
  (let [{:game/keys [state ruleset]} game]
    (loop [s state]
      (if (= :game-over (state/status s))
        (do
          (println "Game over.")
          (println "Results: " (:game/results s)))
        (do
          (println "Applying ruleset for phase " (state/phase s))
          (let [next-state (rules/apply-ruleset s ruleset)]
            (recur next-state)))))))
