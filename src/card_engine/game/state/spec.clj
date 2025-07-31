(ns card-engine.game.state.spec
  (:require
   [clojure.spec.alpha :as s]
   [card-engine.player.spec]
   [card-engine.deck.spec]))

(s/def ::optional-id (s/nilable uuid?))

(s/def :deck/draw-pile :deck/deck)
(s/def :deck/discard-pile (s/coll-of :card/card))

(s/def :game/type keyword?)
(s/def :game/status keyword?)
(s/def :game/phase keyword?)
(s/def :game/players (s/coll-of :player/player))
(s/def :game/current-player-id ::optional-id)
(s/def :game/deck-state (s/keys :req [:deck/draw-pile
                                      :deck/discard-pile]))
(s/def :game/table-state map?)
(s/def :game/state (s/keys :req [:game/type
                                 :game/status
                                 :game/phase
                                 :game/players
                                 :game/current-player-id
                                 :game/deck-state
                                 :game/table-state]))

(defn validate-game-state
  "Checks if the given game state is valid. If the game state is not valid,
  returns an error object. If the game state is valid, returns nil."
  [game-state]
  (if (s/valid? :game/state game-state)
    nil
    [{:type :invalid-game-state
      :value game-state
      :message "Invalid game state."
      :spec (s/explain-str :game/state game-state)}]))
