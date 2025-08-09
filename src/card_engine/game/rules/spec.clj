;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.rules.spec
  (:require
   [clojure.spec.alpha :as s]))

;; Rule Parts
(s/def :rule/type #{:apply :if-then :if-then-else :cond})
(s/def :rule/condition (s/keys :req [:condition/type :condition/params]))
(s/def :rule/action (s/keys :req [:action/type]
                            :opt [:action/params]))
(s/def :rule/else (s/keys :req [:rule/action]))
(s/def :rule/clauses (s/coll-of (s/keys :req [:rule/condition :rule/action])))

;; Single Rule Spec
(s/def ::rule
  (s/keys :req [:rule/id :rule/desc :rule/type :rule/action]
          :opt [:rule/condition :rule/else :rule/clauses]))

;; Ruleset Phase Spec
(s/def :ruleset/phases (s/map-of keyword? (s/coll-of ::rule :kind vector?)))

(s/def :rules/ruleset (s/keys :req [:game/type :ruleset/phases]))

(defn validate-ruleset
  "Checks if the given ruleset is valid. If the ruleset is not valid, returns a
  sequence of error objects. If the ruleset is valid, returns nil."
  [ruleset]
  (cond-> []
    (not (s/valid? :rules/ruleset ruleset)) (conj {:type :invalid-ruleset
                                                   :value ruleset
                                                   :message "Invalid ruleset"
                                                   :spec (s/explain-str :rules/ruleset ruleset)})
    :else seq))
