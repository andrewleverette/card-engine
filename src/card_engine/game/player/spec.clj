(ns card-engine.game.player.spec
  (:require
   [clojure.spec.alpha :as s]))

(s/def :player/id uuid?)
(s/def :player/name string?)
(s/def :player/hand (s/coll-of :card/card))
(s/def :player/score int?)
(s/def :player/status #{:active :inactive})
(s/def :player/is-dealer? boolean?)

(s/def :player/player (s/keys :req [:player/id
                                    :player/name
                                    :player/hand
                                    :player/score
                                    :player/status
                                    :player/is-dealer?]))
