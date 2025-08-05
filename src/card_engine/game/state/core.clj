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
        game-state {:game/type game-type
                    :game/status :setup
                    :game/phase :setup
                    :game/players players
                    :game/current-player-id nil
                    :game/deck-state {:deck/draw-pile shuffled-deck
                                      :deck/discard-pile []}
                    :game/table-state {}}]
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
  (:game/players game-state))

(defn non-dealer-players
  [game-state]
  (->> (players game-state)
       (filter #(not (player/is-dealer? %)))))

(defn player
  [game-state player-id]
  (->> (players game-state)
       (map-indexed vector)
       (filter (fn [[_ player]] (= player-id (player/id player))))
       first))

(defn current-player
  [game-state]
  (when-let [player-id (:game/current-player-id game-state)]
    (->> (players game-state)
         (map-indexed vector)
         (filter (fn [[_ player]] (= player-id (player/id player))))
         first)))

(defn dealer
  [game-state]
  (->> (players game-state)
       (map-indexed vector)
       (filter (fn [[_ player]] (player/is-dealer? player)))
       first))

(defn deck-state
  [game-state]
  (:game/deck-state game-state))

(defn table-state
  [game-state]
  (:game/table-state game-state))

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
