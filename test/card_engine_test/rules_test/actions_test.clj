(ns card-engine-test.rules-test.actions-test
  (:require
   [card-engine.card.interface :as card]
   [card-engine.deck.interface :as deck]
   [card-engine.game.rules.actions :refer [action-params action-type
                                           apply-action]]
   [card-engine.game.state.interface :as state]
   [card-engine.player.interface :as player]
   [clojure.test :refer [deftest is testing]]))

;; --- Helper functions for test setup ---
(def initial-deck
  (deck/shuffle-deck (deck/make-deck)))

(def players [(player/make-player "Player One")
              (player/make-player "Player Two")
              (player/set-dealer-status (player/make-player "Dealer") true)])

(def game-state
  (let [[p1 p2 dealer] players]
    {:game/players (into {} (map (fn [p] [(player/id p) p]) [p1 p2 dealer]))
     :game/current-player-id nil
     :game/dealer-id (player/id dealer)
     :game/deck-state {:deck/draw-pile initial-deck}}))

;; --- Tests for helper functions ---

(deftest action-helpers-test
  (let [action {:rule/action {:action/type :test-action-type
                              :action/params {:value "test-value"}}}]
    (testing "It can return the action type"
      (is (not= nil (action-type action))))
    (testing "It can return the action params"
      (is (not= nil (action-params action))))))

;; --- Tests for `apply-action` multimethod ---

(deftest apply-action-transition-game-status-test
  (testing "It can transition the game status to a new status"
    (let [action {:rule/action {:action/type :transition-game-status
                                :action/params {:status :test-status}}}
          expected [[:state/assoc :game/status :test-status]]]
      (is (= expected (apply-action game-state action))))))

(deftest apply-action-transition-game-phase-test
  (testing "It can transition the game phase to a new phase"
    (let [action {:rule/action {:action/type :transition-phase
                                :action/params {:phase :test-phase}}}
          expected [[:state/assoc :game/phase :test-phase]]]
      (is (= expected (apply-action game-state action))))))

(player/id (first players))
(state/set-current-player game-state (player/id (first players)))

(deftest apply-action-transition-player
  (let [p1-id (player/id (first players))
        p2-id (player/id (second players))
        game-state-with-first-player-as-current (state/set-current-player game-state p1-id)
        game-state-with-second-player-as-current (state/set-current-player game-state p2-id)
        action {:rule/action {:action/type :transition-player}}]
    (testing "It can transition to the first player if no current player is set"
      (is (= [[:state/assoc :game/current-player-id p1-id]]
             (apply-action game-state action))))
    (testing "It can transition to the next player if a current player is set"
      (is (= [[:state/assoc :game/current-player-id p2-id]]
             (apply-action game-state-with-first-player-as-current action))))
    (testing "It can transition to the next player skipping the dealer and wrapping around"
      (is (= [[:state/assoc :game/current-player-id p1-id]]
             (apply-action game-state-with-second-player-as-current action))))))

(deftest apply-action-transition-dealer
  (let [dealer-id (player/id (nth players 2))
        action {:rule/action {:action/type :transition-dealer}}]
    (testing "It can transition the dealer to be the current player"
      (is (= [[:state/assoc :game/current-player-id dealer-id]]
             (apply-action game-state action))))))

(deftest apply-action-score-player-hand
  (testing "It can score a players's blackjack hand"
    (let [action {:rule/action {:action/type :score-player-hand}}
          p1 (first players)
          p1' (player/add-cards p1 [(card/make-card :ace :hearts) (card/make-card :king :hearts)])
          blackjack-game (-> game-state
                             (assoc-in [:game/players (player/id p1)] p1')
                             (assoc :game/current-player-id (player/id p1))
                             (assoc :game/type :blackjack))]
      (is (= [[:state/assoc-in [:game/players (player/id p1)] (player/set-score p1' 21)]]
             (apply-action blackjack-game action))))))

(deftest apply-action-score-dealer-hand
  (testing "It can score a dealer's blackjack hand"
    (let [action {:rule/action {:action/type :score-player-hand}}
          p1 (first players)
          p1' (player/add-cards p1 [(card/make-card :ace :hearts) (card/make-card :king :hearts)])
          blackjack-game (-> game-state
                             (assoc-in [:game/players (player/id p1)] p1')
                             (assoc :game/current-player-id (player/id p1))
                             (assoc :game/type :blackjack))]
      (is (= [[:state/assoc-in [:game/players (player/id p1)] (player/set-score p1' 21)]]
             (apply-action blackjack-game action))))))

(deftest apply-action-calculate-results
  (let [[p1 p2 dealer] players
        p1' (-> p1
                (player/add-cards [(card/make-card :ace :hearts) (card/make-card :king :hearts)])
                (player/set-score 21))
        p2' (-> p2 (player/add-cards [(card/make-card 7 :hearts) (card/make-card :king :hearts) (card/make-card 5 :spades)])
                (player/set-score 22)
                (player/set-status :busted))
        dealer' (-> dealer
                    (player/add-cards [(card/make-card 7 :hearts) (card/make-card :king :hearts)])
                    (player/set-score 17))
        game-state' (assoc game-state
                           :game/players (into {} (map (fn [p] [(player/id p) p]) [p1' p2' dealer']))
                           :game/type :blackjack)
        expected [[:state/assoc :game/results {:win [{:results/player-id (player/id p1')
                                                      :results/player-name (player/player-name p1')
                                                      :results/hand (player/hand p1')
                                                      :results/score (player/score p1')
                                                      :results/status (player/status p1')}]
                                               :lose [{:results/player-id (player/id p2')
                                                       :results/player-name (player/player-name p2')
                                                       :results/hand (player/hand p2')
                                                       :results/score (player/score p2')
                                                       :results/status (player/status p2')}]
                                               :tie []
                                               :dealer {:results/player-id (player/id dealer')
                                                        :results/player-name (player/player-name dealer')
                                                        :results/hand (player/hand dealer')
                                                        :results/score (player/score dealer')
                                                        :results/status (player/status dealer')}}]]
        action {:rule/action {:action/type :calculate-results}}]
    (testing "It can calculate the results of the game"
      (is (= expected (apply-action game-state' action))))))

(deftest apply-action-unknown-action
  (let [action {:rule/action {:action/type :unknown-action}}]
    (testing "It returns an error if the action type is unknown"
      (is (= [[:game/handle-error {:type :apply-action
                                   :message "Failed to apply action"
                                   :errors [{:type :unknown-action-type
                                             :message "Unknown action type"
                                             :value :unknown-action}]}]]
             (apply-action game-state action))))))
