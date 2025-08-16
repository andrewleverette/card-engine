;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine-test.rules-test.comparisons-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [card-engine.game.rules.comparisons :refer [comparison]]))

(deftest comparison-test
  (testing "The := operator returns true for equal values"
    (is (comparison := 1 1 1))
    (is (comparison := "a" "a"))
    (is (not (comparison := 1 2 1))))

  (testing "The :!= operator returns true for not equal values"
    (is (comparison :!= 1 2 3))
    (is (comparison :!= "a" "b"))
    (is (not (comparison :!= 1 1))))

  (testing "The :< operator returns true for strictly increasing values"
    (is (comparison :< 1 2 3))
    (is (not (comparison :< 1 2 2)))
    (is (not (comparison :< 3 2 1))))

  (testing "The :<= operator returns true for non-decreasing values"
    (is (comparison :<= 1 2 3))
    (is (comparison :<= 1 1 2))
    (is (not (comparison :<= 2 1 3))))

  (testing "The :> operator returns true for strictly decreasing values"
    (is (comparison :> 3 2 1))
    (is (not (comparison :> 3 2 2)))
    (is (not (comparison :> 1 2 3))))

  (testing "The :>= operator returns true for non-increasing values"
    (is (comparison :>= 3 2 1))
    (is (comparison :>= 3 3 2))
    (is (not (comparison :>= 1 2 3))))

  (testing "The default case returns true for an unknown operator"
    (is (comparison :unknown 1 2 3))))
