(ns card-engine-test.deck-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [card-engine.card.spec :as card-spec]
   [card-engine.card.interface :as card]
   [card-engine.deck.interface :as deck]))

(defn- make-all-cards
  []
  (into [] (for [suit card-spec/default-suits-order
                 rank card-spec/default-ranks-order]
             (card/make-card rank suit))))

(deftest deck-constructor-and-seclectors
  (testing "deck can be created successfully"
    (let [deck (deck/make-deck)
          expected {:deck/cards (make-all-cards)}]
      (is (= expected deck))))
  (testing "deck properties are accessible"
    (is (= 52 (count (deck/cards (deck/make-deck)))))))

(deftest dealing-cards
  (testing "dealing a single cards from an empty deck"
    (is (= {:dealt nil :remaining {:deck/cards []} :status :deck-empty}
           (deck/deal-card {:deck/cards []}))))
  (testing "dealing a single card from a non-empty deck"
    (is (= {:dealt (card/make-card :ace :spades) :remaining {:deck/cards []}}
           (deck/deal-card {:deck/cards [(card/make-card :ace :spades)]}))))
  (testing "dealing multiple cards from an empty deck"
    (is (= {:dealt [] :remaining {:deck/cards []} :status :deck-empty}
           (deck/deal-cards {:deck/cards []} 5))))
  (testing "dealing multiple cards from a non-empty deck with enough cards"
    (is (= {:dealt [(card/make-card :ace :spades)
                    (card/make-card :two :spades)
                    (card/make-card :three :spades)]
            :remaining {:deck/cards [(card/make-card :four :spades)]}}
           (deck/deal-cards {:deck/cards [(card/make-card :ace :spades)
                                          (card/make-card :two :spades)
                                          (card/make-card :three :spades)
                                          (card/make-card :four :spades)]}
                            3))))
  (testing "dealing more cards than are in the deck"
    (is (= {:dealt [(card/make-card :ace :spades)]
            :remaining {:deck/cards []}
            :status :deck-empty}
           (deck/deal-cards {:deck/cards [(card/make-card :ace :spades)]} 5)))))

