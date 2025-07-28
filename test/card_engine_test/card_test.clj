(ns card-engine-test.card-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [card-engine.card.interface :as card]
   [card-engine.card.spec :as spec]))

(deftest card-constructor-and-seclectors
  (testing "card can be created successfully"
    (is (= {:card/rank :ace :card/suit :spades}
           (card/make-card :ace :spades)))
    (is (= {:card/rank 2 :card/suit :hearts}
           (card/make-card 2 :hearts)))
    (is (= {:card/rank :king :card/suit :clubs}
           (card/make-card :king :clubs)))
    (is (= {:card/rank 10 :card/suit :diamonds}
           (card/make-card 10 :diamonds))))
  (testing "card properties are accessible"
    (let [card (card/make-card :ace :spades)]
      (is (= :ace (card/rank card)))
      (is (= :spades (card/suit card))))))

(deftest card-comparison-tests
  (testing "card default suit comparisions are correct"
    (is (zero? (card/suit-comparator :spades :spades)))
    (is (neg? (card/suit-comparator :hearts :spades)))
    (is (pos? (card/suit-comparator :spades :hearts))))
  (testing "card default rank comparisons are correct"
    (is (zero? (card/rank-comparator :ace :ace)))
    (is (zero? (card/rank-comparator :king :king)))
    (is (zero? (card/rank-comparator 10 10)))
    (is (neg? (card/rank-comparator :ace :king)))
    (is (neg? (card/rank-comparator :jack :queen)))
    (is (neg? (card/rank-comparator 9 10)))
    (is (pos? (card/rank-comparator :king :ace)))
    (is (pos? (card/rank-comparator :queen :jack)))
    (is (pos? (card/rank-comparator 10 9))))
  (testing "card default comparisons are correct"
    (let [ace (card/make-card :ace :spades)
          ace' (card/make-card :ace :clubs)
          face (card/make-card :queen :hearts)
          face' (card/make-card :king :diamonds)
          numeric (card/make-card 10 :clubs)
          numeric' (card/make-card 9 :hearts)]
      (is (pos? (card/card-comparator ace ace')))
      (is (neg? (card/card-comparator ace' ace)))
      (is (pos? (card/card-comparator face face')))
      (is (neg? (card/card-comparator face' face)))
      (is (pos? (card/card-comparator numeric' numeric)))
      (is (neg? (card/card-comparator numeric numeric'))))))

(deftest card-validation
  (testing "invalid cards cannot be created"
    (is (thrown? Exception (card/make-card :foo :bar)))
    (is (thrown? Exception (card/make-card :ace :foo)))
    (is (thrown? Exception (card/make-card :foo :spades))))
  (testing "validation errors are returned"
    (let [rank-errors (spec/validate-card :foo :spades)
          suit-errors (spec/validate-card :ace :foo)
          both-errors (spec/validate-card :foo :bar)]
      (is (= [:invalid-rank] (map #(:type %) rank-errors)))
      (is (= [:invalid-suit] (map #(:type %) suit-errors)))
      (is (= [:invalid-rank :invalid-suit] (map #(:type %) both-errors))))))

(deftest card-string-representation
  (testing "all cards can be represented as strings"
    (let [expected (for [rank ["Ace" "2" "3" "4" "5" "6" "7" "8" "9" "10" "Jack" "Queen" "King"]
                         suit ["Hearts" "Diamonds" "Clubs" "Spades"]]
                     (str rank " of " suit))
          actual (for [rank [:ace 2 3 4 5 6 7 8 9 10 :jack :queen :king]
                       suit [:hearts :diamonds :clubs :spades]]
                   (card/->str (card/make-card rank suit)))]
      (is (= expected actual)))))

(deftest card-sorting
  (testing "cards can be sorted"
    (let [original (for [suit spec/default-suits-order
                         rank spec/default-ranks-order]
                     (card/make-card rank suit))
          shuffled (shuffle original)]
      (is (= original (sort card/card-comparator shuffled))))))
