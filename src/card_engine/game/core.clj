(ns card-engine.game.core
  (:require
   [card-engine.game.rules.interface :as rules]
   [card-engine.game.state.interface :as state]))

(defn make-game
  [ruleset-name players]
  (let [ruleset (rules/load-rulset ruleset-name)
        game-type (:game/type ruleset)
        game-state (state/make-game-state game-type players)]
    {:game/state game-state
     :game/ruleset ruleset}))

(defn game-loop
  [game]
  (let [{:game/keys [state ruleset]} game]
    (loop [s state]
      (if (= :game-over (state/status s))
        (do
          (println "Game over")
          s)
        (let [next-state (rules/apply-ruleset s ruleset)]
          (recur next-state))))))
