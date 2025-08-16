;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine-test.rules-test.conditions-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [card-engine.game.rules.conditions :refer [condition-type condition-params check-condition]]
   [card-engine.player.interface :as player]))

(def players [(player/set-status (player/make-player "Player One") :playing)
              (player/make-player "Player Two")
              (player/set-dealer-status (player/make-player "Dealer") true)])

(def game-state
  (let [[p1 p2 dealer] players]
    {:game/status :setup
     :game/phase :setup
     :game/players (into {} (map (fn [p] [(player/id p) p]) [p1 p2 dealer]))
     :game/current-player-id (player/id p1)
     :game/dealer-id (player/id dealer)}))

;; --- Tests for helper functions ---
(deftest condtion-helpers-test
  (let [rule {:rule/condition {:condition/type :test-type
                               :condition/params {:test-param "test-value"}}}]
    (testing "It can return the condition type"
      (is (not= nil (condition-type rule))))
    (testing "It can return the condition params"
      (is (not= nil (condition-params rule))))))

;; --- Tests for `check-condition` multimethod ---

(deftest check-condition-game-status-matches
  (let [matching-rule {:rule/condition {:condition/type :game-status-matches
                                        :condition/params {:status :setup}}}
        non-matching-rule {:rule/condition {:condition/type :player-status-matches
                                            :condition/params {:status :game-over}}}]
    (testing "It returns true if the player status matches the given status"
      (is (true? (check-condition game-state matching-rule)))
      (is (false? (check-condition game-state non-matching-rule))))))

(deftest check-condition-phase-matches
  (let [matching-rule {:rule/condition {:condition/type :game-phase-matches
                                        :condition/params {:phase :setup}}}
        non-matching-rule {:rule/condition {:condition/type :game-phase-matches
                                            :condition/params {:phase :game-over}}}]
    (testing "It returns true if the game phase matches the given phase"
      (is (true? (check-condition game-state matching-rule)))
      (is (false? (check-condition game-state non-matching-rule))))))

(deftest check-condition-player-status-matches
  (let [matching-rule {:rule/condition {:condition/type :player-status-matches
                                        :condition/params {:status :playing}}}
        non-matching-rule {:rule/condition {:condition/type :player-status-matches
                                            :condition/params {:status :done}}}]
    (testing "It returns true if the player status matches the given status"
      (is (true? (check-condition game-state matching-rule)))
      (is (false? (check-condition game-state non-matching-rule))))))

(deftest check-condition-player-status-in-set
  (let [matching-rule {:rule/condition {:condition/type :player-status-in-set?
                                        :condition/params {:set #{:active :playing}}}}
        non-matching-rule {:rule/condition {:condition/type :player-status-in-set?
                                            :condition/params {:set #{:done :win}}}}]
    (testing "It returns true if the player status is in the given set"
      (is (true? (check-condition game-state matching-rule)))
      (is (false? (check-condition game-state non-matching-rule))))))

(deftest check-condition-all-players-status-in-set
  (let [matching-rule {:rule/condition {:condition/type :all-players-status-in-set?
                                        :condition/params {:set #{:active :playing}}}}
        non-matching-rule {:rule/condition {:condition/type :all-players-status-in-set?
                                            :condition/params {:set #{:done :win}}}}]
    (testing "It returns true if all players status are in the given set"
      (is (true? (check-condition game-state matching-rule)))
      (is (false? (check-condition game-state non-matching-rule))))))

(deftest check-condition-game-over-condition-met
  (let [game-over-state (assoc game-state
                               :game/players
                               (into {} (map (fn [p] [(player/id p) (player/set-status p (rand-nth [:win :lose :tie]))]) players)))
        matching-rule {:rule/condition {:condition/type :game-over-condition-met?}}]
    (testing "It returns true if all players turns are complete and the game is over"
      (is (true? (check-condition game-over-state matching-rule)))
      (is (false? (check-condition game-state matching-rule))))))

(deftest check-condition-unknown-condtion-type
  (testing "It returns true if the condition type is unknown"
    (is (true? (check-condition game-state :unknown-condition-type)))))
