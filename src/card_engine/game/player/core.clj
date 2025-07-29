(ns card-engine.game.player.core)

(defn make-player
  "Returns a new player object with the given name."
  [player-name]
  {:player/id (java.util.UUID/randomUUID)
   :player/name player-name
   :player/hand []
   :player/score 0
   :player/status :active
   :player/is-dealer? false})

(defn id
  [player]
  (:player/id player))

(defn player-name
  [player]
  (:player/name player))

(defn hand
  [player]
  (:player/hand player))

(defn set-hand
  [player hand]
  (assoc player :player/hand (fn [_] hand)))

(defn add-card
  [player card]
  (update player :player/hand conj card))

(defn score
  [player]
  (:player/score player))

(defn set-score
  [player score]
  (assoc player :player/score score))

(defn status
  [player]
  (:player/status player))

(defn set-status
  [player status]
  (assoc player :player/status status))

(defn is-dealer?
  [player]
  (:player/is-dealer? player))

(defn set-dealer-status
  [player is-dealer?]
  (assoc player :player/is-dealer? is-dealer?))
