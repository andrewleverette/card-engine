;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.player.core
  (:require
   [card-engine.player.spec :as spec]))

;; --- Constructors ---

(defn make-player
  "Returns a new player object with the given name."
  [player-name]
  (let [player {:player/id (java.util.UUID/randomUUID)
                :player/name player-name
                :player/hand []
                :player/score 0
                :player/status :active
                :player/is-dealer? false}]
    (if-let [errors (spec/validate-player player)]
      (throw (ex-info "Invalid player" {:type :make-player
                                        :errors errors}))
      player)))

;; --- Selectors

(defn id
  [player]
  (:player/id player))

(defn player-name
  [player]
  (:player/name player))

(defn hand
  [player]
  (:player/hand player))

(defn score
  [player]
  (get player :player/score 0))

(defn status
  [player]
  (:player/status player))

(defn is-dealer?
  [player]
  (:player/is-dealer? player))

;; --- Mutators ---

(defn set-hand
  [player hand]
  (assoc player :player/hand hand))

(defn add-card
  [player card]
  (update player :player/hand conj card))

(defn set-score
  [player score]
  (assoc player :player/score score))

(defn set-status
  [player status]
  (assoc player :player/status status))

(defn set-dealer-status
  [player is-dealer?]
  (assoc player :player/is-dealer? is-dealer?))

(defn reset-player
  [player]
  (-> player
      (set-hand [])
      (set-score 0)
      (set-status :active)))

;; --- String Representation ---

(defn ->str
  [player]
  (str (player-name player) " - " (id player)))

(defn ->short-str
  [player]
  (player-name player))
