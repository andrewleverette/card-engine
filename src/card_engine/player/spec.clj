;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.player.spec
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

(defn validate-player
  "Checks if the given player is valid. If the player is not valid, returns a
  sequence of error objects. If the player is valid, returns nil."
  [player]
  (let [{:player/keys [name hand score status is-dealer?]} player]
    (cond-> []
      (not (s/valid? :player/name name)) (conj {:type :invalid-player-name
                                                :value name
                                                :message (str "Player name '" name "' is not valid.")
                                                :spec (s/explain-str :player/name name)})
      (not (s/valid? :player/hand hand)) (conj {:type :invalid-player-hand
                                                :value hand
                                                :message "Player hand contains an invalid card"
                                                :spec (s/explain-str :player/hand hand)})
      (not (s/valid? :player/score score)) (conj {:type :invalid-player-score
                                                  :value score
                                                  :message (str "Player score '" score "' is not valid.")
                                                  :spec (s/explain-str :player/score score)})
      (not (s/valid? :player/status status)) (conj {:type :invalid-player-status
                                                    :value status
                                                    :message (str "Player status '" status "' is not valid.")
                                                    :spec (s/explain-str :player/status status)})
      (not (s/valid? :player/is-dealer? is-dealer?)) (conj {:type :invalid-player-is-dealer?
                                                            :value is-dealer?
                                                            :message (str "Player is-dealer? '" is-dealer? "' is not valid.")
                                                            :spec (s/explain-str :player/is-dealer? is-dealer?)})
      :else seq)))
