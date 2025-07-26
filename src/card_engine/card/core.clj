(ns card-engine.card.core
  (:require
   [clojure.string :as str]
   [card-engine.card.spec :as spec]))

(defn make-card
  [rank suit]
  (if-let [errors (spec/validate-card rank suit)]
    (throw (ex-info "Invalid card form" {:type :make-card
                                         :errors errors}))
    {:card/rank rank :card/suit suit}))

(defn rank [card]
  (:card/rank card))

(defn- rank->str
  [rank]
  (if (keyword? rank)
    (str/capitalize (name rank))
    (str rank)))

(defn- suit->str
  [suit]
  (str/capitalize (name suit)))

(defn suit [card]
  (:card/suit card))

(defn ->str
  [card]
  (let [r (rank card)
        s (suit card)]
    (str (rank->str r) " of " (suit->str s))))
