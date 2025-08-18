;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.core
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [card-engine.game.state.interface :as state]
   [card-engine.game.rules.spec :refer [validate-ruleset]]
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
          (if errors
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

(defn rule-type
  [rule]
  (:rule/type rule))

(defmulti apply-rule
  (fn [_ rule] (rule-type rule)))

(defmethod apply-rule :apply
  [game-state rule]
  (apply-action game-state rule))

(defmethod apply-rule :if-then
  [game-state rule]
  (if (check-condition game-state rule)
    (apply-action game-state rule)
    []))

(defmethod apply-rule :if-then-else
  [game-state rule]
  (if (check-condition game-state rule)
    (apply-action game-state rule)
    (apply-action game-state (:rule/else rule))))

(defmethod apply-rule :cond
  [game-state rule]
  (loop [[clause & clauses] (:rule/clauses rule)]
    (cond
      (nil? clause) (if-let [else (:rule/else rule)]
                      (apply-action game-state else)
                      game-state)
      (check-condition game-state clause) (apply-action game-state clause)
      :else (recur clauses))))

(defmethod apply-rule :default
  [_ rule]
  [[:game/handle-error {:type :apply-rule
                        :message "Failed to apply rule"
                        :errors [{:type :unknown-rule-type
                                  :message "Unknown rule type"
                                  :value (rule-type rule)}]}]])
