# Clojure Card Engine

(WORK IN PROGRESS)

## Overview

This project is a modular and data-driven engine for building card games in Clojure. The core philosophy is to separate game-specific logic from the engine itself. Game rules are defined in external EDN files, making the engine highly configurable and reusable for various card games without any code changes.

The engine uses immutable data structures, making game state management predictable and safe. Its extensible architecture, powered by Clojure's multimethods, allows for new rule types and actions to be added easily.

## Core Features

* **Data-Driven Rules:** Game logic is defined in EDN files, which are read and applied by the engine. This allows for rapid prototyping and easy modification of game rules.
* **Modular Design:** The project is organized into distinct namespaces for cards, decks, players, game state, and the rules engine, promoting a clear separation of concerns.
* **Extensible Rules Engine:** Using Clojure's multimethods, new rule types and actions can be added to the engine without modifying any of the core logic.
* **Pure Functions and Immutability:** The engine operates on immutable data structures that are built with simple maps and vectors.
* **Simple Game Loop:** A recursive game loop orchestrates the entire game, applying rules from the current phase until a game-over condition is met.

## Key Components

* `card-engine.card`: The fundamental building block, responsible for creating and representing individual cards.
* `card-engine.deck`: Manages the creation, shuffling, and dealing of card decks.
* `card-engine.player`: Defines the player data structure, including their hand, score, and status.
* `card-engine.game.state`: The central data structure that holds the entire state of a game at any given moment, including the deck, players, and current phase.
* `card-engine.game.rules`: The heart of the engine. It loads rulesets and uses multimethods to apply conditions and actions to the game state.
* `card-engine.game.core`: Contains the main game loop that drives the game from start to finish.

## How It Works

1.  A game starts by loading an EDN ruleset file (e.g., `blackjack.edn`).
2.  An initial `game-state` is created with a shuffled deck and the players.
3.  The main `game-loop` function begins, continuously checking the game's status.
4.  For each iteration, the loop gets the vector of rules for the current `game-phase` from the ruleset.
5.  It applies each rule in sequence, which may update the `game-state`, deal cards, change the current player, or transition to a new phase.
6.  The loop continues until a rule sets the `game/status` to `:game-over`, at which point the final game state is returned.

## Next Steps / Future Work

* **Ruleset Validation:** Implement a spec to validate ruleset EDN files, ensuring they are correctly structured before the engine attempts to use them.
* **Player Strategies:** Develop a system for algorithmic players and interactive human players to make decisions during their turns.
* **Logging:** Implement logging to track game progress and error states.
* **Interactive Interface:** Build a simple command-line interface for playing games.
* **Game State Tracking:** Add functionality to track game history and game logs.
* **Game State Persistence:** Add functionality to save and load game states.

## License

This project is licensed under the MIT License.
