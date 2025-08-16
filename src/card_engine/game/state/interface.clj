;; Copyright (c) 2025 Andrew Leverette
;; Distributed under the MIT License. See LICENSE.md.

(ns card-engine.game.state.interface
  "Provides a public interface for working with game state objects."
  (:require
   [card-engine.game.state.core :as core]))

;; --- Public Interface ---

;; --- Constructors ---

(defn make-game-state
  "Returns a new game state with default values.
  
  Args:
  * game-type: The type of game to create.
  * players: A list of player ids."
  [game-type players] (core/make-game-state game-type players))

;; --- Selectors ---

(defn game-type
  "Returns the type of the game.
  
  Args:
  * game-state - the game state object"
  [game-state] (core/game-type game-state))

(defn status
  "Returns the status of the game.
  
  Args:
  * game-state - the game state object"
  [game-state] (core/status game-state))

(defn phase
  "Returns the phase of the game.
  
  Args:
  * game-state - the game state object"
  [game-state] (core/phase game-state))

(defn players
  "Returns the list of player objects.

  Args:
  * game-state - the game state object"
  [game-state] (core/players game-state))

(defn non-dealer-players
  "Returns the list of non-dealer player objects.
  
  Args:
  * game-state - the game state object"
  [game-state] (core/non-dealer-players game-state))

(defn player
  "Returns the player object for the given player id.

  Args:
  * game-state - the game state object
  * player-id - the player id"
  [game-state player-id] (core/player game-state player-id))

(defn current-player
  "Returns the player object of the active player for the turn
  
  Args:
  * game-state - the game state object"
  [game-state] (core/current-player game-state))

(defn dealer
  "Returns the player object of the dealer.
  
  Args:
  * game-state - the game state object"
  [game-state] (core/dealer game-state))

(defn deck-state
  "Returns the deck state object.
  
  Args:
  * game-state - the game state object"
  [game-state] (core/deck-state game-state))

(defn table-state
  "Returns the table state object.
  
  Args:
  * game-state - the game state object"
  [game-state] (core/table-state game-state))

(defn pending-prompt
  "Returns the pending prompt object or nil if no prompt is pending.
  
  Args:
  * game-state - the game state object"
  [game-state] (core/pending-prompt game-state))

(defn rule-index
  "Returns the index of the current rule.

  Args:
  * game-state - the game state object"
  [game-state] (core/rule-index game-state))

;; --- Mutators ---

(defn set-status
  "Sets the status of the game.
  
  Args:
  * game-state - the game state object
  * status - the new status of the game"
  [game-state status] (core/set-status game-state status))

(defn set-phase
  "Sets the phase of the game.
  
  Args:
  * game-state - the game state object
  * phase - the new phase of the game"
  [game-state phase] (core/set-phase game-state phase))

(defn set-current-player
  "Sets the current player for the turn.
  
  Args:
  * game-state - the game state object
  * player-id - the id of the player to set as the active player"
  [game-state player-id] (core/set-current-player game-state player-id))

(defn set-deck-state
  "Sets the deck state object.
  
  Args:
  * game-state - the game state object
  * deck-state - the new deck state object"
  [game-state deck-state] (core/set-deck-state game-state deck-state))

(defn set-table-state
  "Sets the table state object.
  
  Args:
  * game-state - the game state object
  * table-state - the new table state object"
  [game-state table-state] (core/set-table-state game-state table-state))

(defn set-pending-prompt
  "Sets the pending prompt object.

  Args:
  * game-state - the game state object
  * prompt - the new pending prompt object"
  [game-state prompt] (core/set-pending-prompt game-state prompt))

(defn set-rule-index
  "Sets the index of the current rule.

  Args:
  * game-state - the game state object
  * idx - the new index of the current rule"
  [game-state idx] (core/set-rule-index game-state idx))
