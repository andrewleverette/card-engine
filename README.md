# Clojure Card Engine

(WORK IN PROGRESS)

## Overview

This project is a modular, data-driven engine for building card games in Clojure. The goal separate game-specific logic from the game engine. Game rules are defined in external EDN files, making the engine highly configurable and reusable for various card games without any code changes.

---

## Core Features

  * **Data-Driven Rules**: Game logic is defined in EDN files, which are read and applied by the engine. This allows for rapid prototyping and easy modification of game rules.
  * **Modular Design**: The project is organized into distinct namespaces for cards, decks, players, game state, and the rules engine, promoting a clear separation of concerns.
  * **Extensible Rules Engine**: Using Clojure's **multimethods**, new rule types and actions can be added to the engine without modifying any of the core logic.
  * **Controlled State**: The game state is managed via an `atom`, providing controlled access to the game's mutable state from an otherwise pure, functional core.
  * **Game Loop via State Machine**: A single, recursive **`game-loop`** acts as a state machine, orchestrating the entire game by applying rules for the current phase until a phase transition or game-over condition is met.

## Key Components

  * `card-engine.card`: Responsible for creating and representing individual cards.
  * `card-engine.deck`: Manages the creation, shuffling, and dealing of immutable card decks.
  * `card-engine.player`: Defines the player data structure, including their hand, score, and status.
  * `card-engine.game.state`: The central data structure that holds the entire state of a game at any given moment, including the deck, players, and current phase.
  * `card-engine.game.rules`: The rules engine. It's responsible loading rulesets from EDN files and applying them to the game state.
  * `card-engine.game.core`: Contains the main `game-loop` that drives the game from start to finish orchestrating state changes and other side-effecting events.

## How It Works

1. A game starts by loading an EDN ruleset file (e.g., `blackjack.edn`).
2. An initial state is created with a shuffled deck and the players.
3. The main `game-loop` function begins, continuously checking the game's status.
4. For each iteration, the loop gets the vector of rules for the current phase from the ruleset.
5. It applies each rule in sequence.
6. If a rule's action changes the game phase, the loop immediately fetches the rules for the new phase and begins processing them. Otherwise, the rules for the current phase will be applied repeatedly. ***(This could case an infinite loop if not careful)***
7. This process continues until a rule sets the game status to `:game-over`.

---

## Creating a Ruleset

A ruleset is an EDN file that contains a map that the engine reads. This map has two main parts:

  * `:game/type`: A keyword that identifies the game (e.g., `:blackjack`). This is used by the multimethods to dispatch to game-specific logic for things like scoring.
  * `:ruleset/phases`: A map that organizes the game's logic into distinct phases. The keys of this map are keywords representing the game phases (e.g., `:setup`, `:player-turn`, `:dealer-turn`), and the values are **vectors of rules** to be applied sequentially during that phase.

### Rule Types

Each rule is a map with a unique `:rule/id`, a `:rule/desc`, and a `:rule/type`. The engine's core logic dispatches on the `:rule/type` to determine how to apply the rule.

#### `apply` Rule Type

The simplest rule type. It has no condition and always applies a single action. This is used for unconditional state changes.

```clojure
{:rule/id :deal-to-players
 :rule/type :apply
 :rule/desc "Deals two cards to each player."
 :rule/action {:action/type :deal
               :action/params {:num-cards 2 :target :all-non-dealers}}}
```

#### `if-then` Rule Type

This rule applies an action only if a condition is met. The `:rule/condition` map specifies the condition to check.

```clojure
{:rule/id :player-hit-action
 :rule/type :if-then
 :rule/desc "If the player's action is 'hit', deal them a card."
 :rule/condition {:condition/type :player-action-matches
                  :condition/params {:action :hit}}
 :rule/action {:action/type :deal
               :action/params {:num-cards 1 :target :current-player}}}
```

#### `if-then-else` Rule Type

Similar to `if-then`, but it applies one action if the condition is met and a different action if it is not. The `:rule/else` map specifies the action that is applied if the condition is not satisfied.

```clojure
{:rule/id :player-score-check
 :rule/type :if-then-else
 :rule/desc "If the player score is > 21, set status to busted, otherwise set to inactive."
 :rule/condition {:condition/type :score-threshold
                  :condition/params {:target :current-player
                                     :threshold 21
                                     :operator :>}}
 :rule/action {:action/type :update-player-status
               :action/params {:status :busted}}
 :rule/else {:action/type :update-player-status
             :action/params {:status :inactive}}}
```

#### `cond` Rule Type

This rule type is similar to Clojure's `cond` macro. It takes a vector of clauses, where each clause is a map with a `:rule/condition` and a `:rule/action`. The engine checks each condition in order and applies the action of the first one that is met. It can also include an optional  `:rule/else` action for a default case.

```clojure
{:rule/id :handle-player-score-and-transition
 :rule/type :cond
 :rule/desc "Checks player score and either sets status or transitions phase."
 :rule/clauses
 [{:rule/condition {:condition/type :score-threshold
                    :condition/params {:target :current-player
                                       :threshold 21
                                       :operator :>}}
   :rule/action {:action/type :update-player-status
                 :action/params {:status :busted}}}
  {:rule/condition {:condition/type :all-players-status-in-set?
                   :condition/params {:set #{:inactive :busted}}}
   :rule/action {:action/type :set-phase
                 :action/params {:phase :dealer-turn}}}
  {:rule/condition {:condition/type :current-player-is-last-player?}
   :rule/action {:action/type :transition-phase
                 :action/params {:phase :dealer-turn}}}]}
```

### Actions

Actions are the effects that drive modifications to the game state. They are executed by the `apply-action` multimethod, which dispatches on the `:action/type` keyword. An action is defined as a map that contains at least the `:action/type` keyword and optionally `:action/params`.

#### Examples

```clojure
;; Deal action
{:action/type :deal
 :action/params {:num-cards 1 :target :current-player}}}

;; Transition phase action
{:action/type :transition-phase
 :acption/params {:phase :player-turn}}}

;; Set next player as current player
;; No params required at the moment
{:action/type :transition-player}
```

### Conditions

Conditions are the checks that determine whether a rule's action should be executed. They are checked by the `check-condition` multimethod, which dispatches on the `:condition/type` keyword. A condition id defined as a map that contains the `:condition/type` and  `:condition/params` keywords.

#### Examples

```clojure

;; Player status matches condition
{:condition/type :player-status-matches
 :condition/params {:status :done}}

;; Player status in set condition
{:condition/type :player-status-in-set?
 :condition/params {:set #{:inactive :busted}}}

;; Game phase matches condition
{:condition/type :game-phase-matches
 :condition/params {:phase :player-turn}}

;; Score threshold condition
{:condition/type :score-threshold
 :condition/params {:target :current-player
                    :threshold 21
                    :operator :>}}
```

---

## Next Steps / Future Work

  * **Game State Persistence**: Add functionality to serialize and deserialize game states, allowing games to be saved and loaded.
  * **Comprehensive `clojure.spec`**: Expand `spec` coverage to validate all aspects of the ruleset and game data, providing robust error-checking during development and runtime.
  * **Player AI/Strategies**: Develop a system for algorithmic players to make decisions during their turns. The current `strategy` and `action` keys in the player state are well-positioned to support this.
  * **Interactive Interface**: Build a more advanced command-line or graphical interface for playing games with human players. The `process-io-events` function in `game.core` is a solid starting point.
  * **Logging and History**: Implement logging to track game progress and a mechanism to store a history of game state changes for replay or analysis.

---

## License

This project is distributed under the MIT License. See `LICENSE.md` for details.
