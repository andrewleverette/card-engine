(ns card-engine.player.interface
  "Provides a public interface for working with player objects."
  (:require
   [card-engine.player.core :as core]))

;; --- Public Interface ---

;; --- Constructors ---

(defn make-player
  "Returns a new player object with the given name.
  
  Args:
  * player-name - the name of the player"
  [player-name] (core/make-player player-name))

;; --- Selectors ---

(defn id
  "Returns the id of the player.

  Args:
  * player - the player to get the id of"
  [player] (core/id player))

(defn player-name
  "Returns the name of the player.

  Args:
  * player - the player to get the name of"
  [player] (core/player-name player))

(defn hand
  "Returns the hand of the player.
  
  Args:
  * player - the player to get the hand of"
  [player] (core/hand player))

(defn score
  "Returns the score of the player.
  
  Args:
  * player - the player to get the score of"
  [player] (core/score player))

(defn status
  "Returns the status of the player.
  
  Args:
  * player - the player to get the status of"
  [player] (core/status player))

(defn is-dealer?
  "Returns whether the player is the dealer.
  
  Args:
  * player - the player to check if it is the dealer of"
  [player] (core/is-dealer? player))

;; --- Mutators ---

(defn set-hand
  "Sets the hand of the player.

  Args:
  * player - the player to set the hand of
  * hand - the hand to set the player's hand to"
  [player hand] (core/set-hand player hand))

(defn add-card
  "Adds a card to the player's hand.

  Args:
  * player - the player to add the card to
  * card - the card to add to the player's hand"
  [player card] (core/add-card player card))

(defn set-score
  "Sets the score of the player.
  
  Args:
  * player - the player to set the score of
  * score - the score to set the player's score to"
  [player score] (core/set-score player score))

(defn set-status
  "Sets the status of the player.

  Args:
  * player - the player to set the status of
  * status - the status to set the player's status to"
  [player status] (core/set-status player status))

(defn set-dealer-status
  "Sets whether the player is the dealer.

  Args:
  * player - the player to set the dealer status of
  * is-dealer? - whether the player is the dealer"
  [player is-dealer?] (core/set-dealer-status player is-dealer?))

(defn reset-player
  "Resets the player's hand, score, and status
  to their default values.

  Args:
  * player - the player to reset"
  [player] (core/reset-player player))

;; --- String Representation ---

(defn ->str
  "Returns a string representation of the player.
  
  Args:
  * player - the player to get the string representation of"
  [player]
  (core/->str player))

(defn ->short-str
  "Returns a short string representation of the player.
  
  Args:
  * player - the player to get the short string representation of"
  [player]
  (core/->short-str player))
