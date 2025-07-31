(ns card-engine.game.rules.core
  (:require
   [card-engine.deck.interface :as deck]
   [card-engine.player.interface :as player]
   [card-engine.game.state.interface :as state]
   [clojure.edn :as edn]
   [clojure.java.io :as io]))

(defn load-ruleset
  "Returns the ruleset with the given name.
  Throws an exception if the ruleset is not found."
  [ruleset-name]
  (let [path (str "game_rules/" ruleset-name ".edn")]
    (if-let [source (io/resource path)]
      (with-open [rdr (io/reader source)]
        (edn/read (java.io.PushbackReader. rdr)))
      (throw (ex-info "Ruleset not found" {:type :load-ruleset
                                           :errors [{:type :ruleset-not-found
                                                     :value ruleset-name
                                                     :message "Could not find ruleset file"}]})))))

(defmulti check-condition
  "Checks if the given rule's condition is met.
  Returns true if the condition is met, false otherwise"
  (fn [_ rule] (:rule/condition-type rule)))

(defmethod check-condition :game-phase-matches
  [game-state rule]
  (let [{:rule/keys [condition-params]} rule]
    (= (:game/phase game-state) (:phase condition-params))))

(defmulti apply-action
  "Applies the given action to the game state.
  Returns the new game-state"
  (fn [_ rule] (:rule/action-type rule)))

(defmethod apply-action :deal
  [game-state rule]
  (let [{:rule/keys [action-params]} rule
        {:keys [num-cards from]} action-params
        draw-pile (get-in game-state [:game/deck-state from])
        {:keys [dealt remaining status]} (deck/deal-cards draw-pile num-cards)
        [p-idx p] (state/current-player game-state)
        p' (reduce #(player/add-card %1 %2) p dealt)]
    (-> game-state
        (assoc-in [:game/deck-state from] remaining)
        (assoc-in [:game/deck-state :deck/status] status)
        (assoc-in [:game/players p-idx] p'))))

(defn apply-rule
  "Applies the given rule to the game state if its condition is met.
  Returns the new game-state"
  [game-state rule]
  (if (check-condition game-state rule)
    (apply-action game-state rule)
    game-state))

