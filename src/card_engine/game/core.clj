;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.core
  (:require
   [clojure.string :as str]
   [card-engine.game.rules.interface :as rules]
   [card-engine.game.state.interface :as state]))

(defn make-game
  [ruleset-name players]
  (let [ruleset (rules/load-rulset ruleset-name)
        game-type (:game/type ruleset)
        game-state (atom (state/make-game-state game-type players))]
    {:game/state game-state
     :game/ruleset ruleset}))

(defn process-io-events
  [game-state prompt]
  (let [{:prompt/keys [player-id message actions output-key]} prompt
        result (do
                 (println message)
                 (println "Actions: " (pr-str actions))
                 (print "> ")
                 (flush)
                 (keyword (read-string (read-line))))]
    (apply swap! game-state assoc-in [[:game/players player-id output-key] result])))

(defn process-state-changes
  [game-state changes]
  (doseq [[action & args] changes]
    (case action
      :state/assoc (apply swap! game-state assoc args)
      :state/assoc-in (apply swap! game-state assoc-in args)
      :game/handle-io (process-io-events game-state args)
      :game/handle-error (throw (ex-info "Game error" args)))))

(defn format-player-results
  [result]
  (let [{:results/keys [player-name score status]} result]
    (if (= :busted status)
      (str player-name " (busted) -> " score)
      (str player-name " -> " score))))

(defn print-game-over-message
  [game-state]
  (let [results (:game/results @game-state)
        winners (str/join ", " (map format-player-results (:win results)))
        losers (str/join ", " (map format-player-results (:lose results)))
        ties (str/join ", " (map format-player-results (:tie results)))]
    (println "Game over")
    (println "Dealer: " (format-player-results (:dealer results)))
    (println "Winners: " (if (seq winners) winners "None"))
    (println "Losers: " (if (seq losers) losers "None"))
    (println "Ties: " (if (seq ties) ties "None"))))

(defn game-loop
  [game]
  (let [{:game/keys [state ruleset]} game]
    (loop [phase (state/phase @state)
           rules (get-in ruleset [:ruleset/phases phase])]
      (if (= :game-over (state/status @state))
        (print-game-over-message state)
        (if-let [rule (first rules)]
          (let [changes (rules/apply-rule @state rule)]
            (process-state-changes state changes)
            (if (not= phase (state/phase @state))
              (recur (state/phase @state) (get-in ruleset [:ruleset/phases (state/phase @state)]))
              (recur phase (rest rules))))
          ;; Start rules from beginning of phase
          (recur phase (get-in ruleset [:ruleset/phases phase])))))))
