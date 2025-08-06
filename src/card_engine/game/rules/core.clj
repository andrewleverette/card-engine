;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.core
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [card-engine.game.rules.spec :refer [validate-ruleset]]
   [card-engine.game.state.interface :as state]
   [card-engine.game.rules.actions :refer [apply-action]]
   [card-engine.game.rules.conditions :refer [check-condition]]))

(defn load-ruleset
  "Returns the ruleset with the given name.
  Throws an exception if the ruleset is not found."
  [ruleset-name]
  (let [path (str "game_rules/" ruleset-name ".edn")]
    (if-let [source (io/resource path)]
      (with-open [rdr (io/reader source)]
        (let [ruleset (edn/read (java.io.PushbackReader. rdr))
              errors (validate-ruleset ruleset)]
          (if (seq errors)
            (throw (ex-info "Invalid ruleset" {:type :load-ruleset
                                               :errors errors}))
            ruleset)))
      (throw (ex-info "Ruleset not found" {:type :load-ruleset
                                           :errors [{:type :ruleset-not-found
                                                     :value ruleset-name
                                                     :message "Could not find ruleset file"}]})))))

(defn list-rulesets
  "Returns a list of all available rulesets defined in resources/game_rules."
  []
  (->> (io/file "resources/game_rules")
       file-seq
       (filter #(.isFile %))
       (map #(.getName %))
       (filter #(.endsWith % ".edn"))
       (map #(str/replace % ".edn" ""))))

(defn apply-rule
  "Applies the given rule to the game state if its condition is met.
  Returns the new game-state"
  [game-state rule]
  (if (check-condition game-state rule)
    (apply-action game-state rule)
    game-state))

(defn apply-ruleset
  "Applies the rules in the given ruleset to the game state."
  [game-state ruleset]
  (let [phase (state/phase game-state)
        rules (get-in ruleset [:ruleset/phases phase])]
    (if (seq rules)
      (reduce apply-rule game-state rules)
      (throw (ex-info "Failed to apply ruleset" {:type :apply-ruleset
                                                 :errors [{:type :no-rules-for-phase
                                                           :value phase
                                                           :message "No rules found for phase"}]})))))
