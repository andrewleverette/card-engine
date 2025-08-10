(ns card-engine.core
  (:gen-class)
  (:require
   [card-engine.game.interface :as game]
   [card-engine.player.interface :as player]))

(defn -main
  []
  (let [player1 (player/make-player "Player 1")
        player2 (-> "Dealer" player/make-player (player/set-dealer-status true))
        game (game/make-game "blackjack" [player1 player2])]
    (try
      (game/game-loop game)
      (catch Exception e
        (println "Error: " (ex-message e))
        (println "Error Data: " (ex-data e))))))
