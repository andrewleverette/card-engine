;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.state.core
  (:require
   [card-engine.deck.interface :as deck]
   [card-engine.player.interface :as player]
   [card-engine.game.state.spec :as spec]))

;; --- Constructors ---

(defn make-game-state
  "Returns a new game state with default values."
  [game-type players]
  (let [shuffled-deck (deck/shuffle-deck (deck/make-deck))
        player-map (into {} (map #(vector (player/id %) %) players))
        dealer (first (filter #(player/is-dealer? %) players))
        game-state {:game/type game-type
                    :game/status :setup
                    :game/phase :setup
                    :game/players player-map
                    :game/current-player-id nil
                    :game/dealer-id (player/id dealer)
                    :game/deck-state {:deck/draw-pile shuffled-deck
                                      :deck/discard-pile []}
                    :game/table-state {}
                    :game/pending-prompt nil
                    :game/rule-index 0}]

    (if-let [errors (spec/validate-game-state game-state)]
      (throw (ex-info "Invalid game state" {:type :make-game-state
                                            :errors errors}))
      game-state)))

;; --- Selectors ---

(defn game-type
  [game-state]
  (:game/type game-state))

(defn status
  [game-state]
  (:game/status game-state))

(defn phase
  [game-state]
  (:game/phase game-state))

(defn players
  [game-state]
  (vec (vals (:game/players game-state))))

(defn non-dealer-players
  [game-state]
  (->> (players game-state)
       (filter #(not (player/is-dealer? %)))))

(defn player
  [game-state player-id]
  (get-in game-state [:game/players player-id]))

(defn current-player
  [game-state]
  (when-let [player-id (:game/current-player-id game-state)]
    (player game-state player-id)))

(defn dealer
  [game-state]
  (when-let [dealer-id (:game/dealer-id game-state)]
    (player game-state dealer-id)))

(defn deck-state
  [game-state]
  (:game/deck-state game-state))

(defn table-state
  [game-state]
  (:game/table-state game-state))

(defn pending-prompt
  [game-state]
  (:game/pending-prompt game-state))

(defn rule-index
  [game-state]
  (:game/rule-index game-state))

;; --- Mutators ---

(defn set-status
  [game-state status]
  (assoc game-state :game/status status))

(defn set-phase
  [game-state phase]
  (assoc game-state :game/phase phase))

(defn set-current-player
  [game-state player-id]
  (assoc game-state :game/current-player-id player-id))

(defn set-deck-state
  [game-state deck-state]
  (assoc game-state :game/deck-state deck-state))

(defn set-table-state
  [game-state table-state]
  (assoc game-state :game/table-state table-state))

(defn set-pending-prompt
  [game-state prompt]
  (assoc game-state :game/pending-prompt prompt))

(defn set-rule-index
  [game-state idx]
  (assoc game-state :game/rule-index idx))
