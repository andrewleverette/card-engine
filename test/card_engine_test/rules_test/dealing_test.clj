;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine-test.rules-test.dealing-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [card-engine.deck.interface :as deck]
   [card-engine.player.interface :as player]
   [card-engine.game.state.interface :as state]
   [card-engine.game.rules.dealing :refer [deal-action]]))

;; --- Helper functions for test setup ---
(def initial-deck
  (deck/shuffle-deck (deck/make-deck)))

(def game-state
  (let [p1 (player/make-player "Player One")
        p2 (player/make-player "Player Two")
        dealer (player/set-dealer-status (player/make-player "Dealer") true)]
    (-> {}
        (assoc :game/players (into {} (map (fn [p] [(player/id p) p]) [p1 p2 dealer]))
               :game/current-player-id (player/id p1)
               :game/dealer-id (player/id dealer))
        (assoc-in [:game/deck-state :deck/draw-pile] initial-deck))))

(defn create-deal-to-many-players-actions
  [draw-pile players num-cards]
  (first (reduce (fn [[actions remaining] p]
                   (let [{:keys [dealt remaining status]} (deck/deal-cards remaining num-cards)
                         p' (player/add-cards p dealt)]
                     [(conj actions
                            [:state/assoc-in [:game/players (player/id p)] p']
                            [:state/assoc-in [:game/deck-state :deck/draw-pile] remaining]
                            [:state/assoc-in [:game/deck-state :deck/status] status])
                      remaining]))
                 [[] draw-pile]
                 players)))

(defn get-player-by-name
  [game-state name]
  (first (filter #(= (player/player-name %) name) (state/players game-state))))

;; --- Tests for `deal-action` multimethod ---
(deftest deal-action-current-player-test
  (let [params {:target :current-player :num-cards 2 :from :deck/draw-pile}
        {:keys [dealt remaining status]} (deck/deal-cards (get-in game-state [:game/deck-state :deck/draw-pile]) 2)
        p1' (reduce #(player/add-card %1 %2) (state/current-player game-state) dealt)
        actions (deal-action game-state params)]
    (testing "It returns the correct actions for dealing to the current player"
      (is (= 3 (count actions)))
      (is (= [[:state/assoc-in [:game/deck-state :deck/draw-pile] remaining]
              [:state/assoc-in [:game/deck-state :deck/status] status]
              [:state/assoc-in [:game/players (player/id p1')] p1']]
             actions)))))

(deftest deal-action-all-players-test
  (let [params {:target :all-players :num-cards 1 :from :deck/draw-pile}
        actions (deal-action game-state params)
        players (state/players game-state)
        draw-pile (get-in game-state [:game/deck-state :deck/draw-pile])
        expected (create-deal-to-many-players-actions draw-pile players 1)]
    (testing "It returns the correct actions for dealing to all players"
      (is (= 9 (count actions)))
      (is (= expected actions)))))

(deftest deal-action-all-non-dealers-test
  (let [params {:target :all-non-dealers :num-cards 1 :from :deck/draw-pile}
        actions (deal-action game-state params)
        non-dealers (state/non-dealer-players game-state)
        draw-pile (get-in game-state [:game/deck-state :deck/draw-pile])
        expected (create-deal-to-many-players-actions draw-pile non-dealers 1)]
    (testing "It returns the correct modifications for all non-dealers"
      (is (= 6 (count actions)))
      (is (= expected actions)))))

(deftest deal-action-dealer-test
  (let [params {:target :dealer :num-cards 1 :from :deck/draw-pile}
        {:keys [dealt remaining status]} (deck/deal-cards (get-in game-state [:game/deck-state :deck/draw-pile]) 1)
        dealer' (player/add-cards (state/dealer game-state) dealt)
        actions (deal-action game-state params)]
    (testing "It returns the correct modifications for the dealer"
      (is (= 3 (count actions)))
      (is (= [[:state/assoc-in [:game/deck-state :deck/draw-pile] remaining]
              [:state/assoc-in [:game/deck-state :deck/status] status]
              [:state/assoc-in [:game/players (player/id dealer')] dealer']]
             actions)))))

(deftest deal-action-no-current-player-test
  (let [params {:target :current-player :num-cards 1 :from :deck/draw-pile}
        game-state-without-current-player (assoc game-state :game/current-player-id nil)
        actions (deal-action game-state-without-current-player params)]
    (testing "It returns an error action that contains a no-current-player error"
      (is (= [[:game/handle-error {:type :apply-deal-action
                                   :message "Failed to apply deal action"
                                   :params params
                                   :errors [{:type :no-current-player
                                             :message "No current player"}]}]]
             actions)))))

(deftest deal-action-no-dealer-test
  (let [params {:target :dealer :num-cards 1 :from :deck/draw-pile}
        game-state-without-dealer (assoc game-state :game/dealer-id nil)
        actions (deal-action game-state-without-dealer params)]
    (testing "It returns an error action that contains a no-dealer error"
      (is (= [[:game/handle-error {:type :apply-deal-action
                                   :message "Failed to apply deal action"
                                   :params params
                                   :errors [{:type :no-dealer
                                             :message "No dealer"}]}]]
             actions)))))

(deftest deal-action-error-case-test
  (let [params {:target :unknown-action}
        actions (deal-action {} params)]
    (testing "It returns an error modification for an unknown target"
      (is (= 1 (count actions)))
      (is (= :game/handle-error (ffirst actions))))))
