(ns card-engine.game.rules.core
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [card-engine.deck.interface :as deck]
   [card-engine.player.interface :as player]
   [card-engine.game.state.interface :as state]
   [card-engine.game.rules.dealing :refer [deal-action]]))

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
  Returns true if the condition is met.
  
  If no dispatcher is found, returns true"
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

(defmulti apply-action
  "Applies the given action to the game state.
  Returns the new game-state"
  (fn [_ rule] (:rule/type rule)))

(defmethod apply-action :deal
  [game-state rule]
  (let [{:rule/keys [action-params]} rule]
    (deal-action game-state action-params)))

(defmethod apply-action :transition-game-status
  [game-state rule]
  (let [{:rule/keys [action-params]} rule]
    (state/set-status game-state (:status action-params))))

(defmethod apply-action :transition-phase
  [game-state rule]
  (let [{:rule/keys [action-params]} rule]
    (state/set-phase game-state (:next-phase action-params))))

(defmethod apply-action :transition-player
  [game-state rule]
  (let [{:rule/keys [_]} rule]
    (let [[p-idx _] (state/current-player game-state)
          players (state/players game-state)
          next-player-idx (if (nil? p-idx) 0 (mod (inc p-idx) (count players)))
          next-player (get players next-player-idx)]
      (if (player/is-dealer? next-player)
        ;; If the next player is a dealer, 
        ;; set the current player to the next non-dealer 
        (let [idx (mod (inc next-player-idx) (count players))
              player (get players idx)]
          (state/set-current-player game-state (player/id player)))
        (state/set-current-player game-state (player/id next-player))))))

(defmethod apply-action :card-management
  [game-state rule]
  (let [{:rule/keys [action-params]} rule]
    (if (= :collect-all-cards (:action action-params))
      (let [players (mapv #(player/reset-player %) (state/players game-state))
            new-deck (deck/shuffle-deck (deck/make-deck))]
        (-> game-state
            (assoc-in [:game/players] players)
            (state/set-deck-state {:deck/draw-pile new-deck
                                   :deck/discard-pile []})
            (state/set-table-state {})
            (state/set-current-player nil)))
      game-state)))

(defmethod apply-action :default
  [game-state _] game-state)

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
