(ns card-engine.game.rules.dealing
  "This namespace defines the functions that deal cards to players.
  
  This is provided by a multimethod that dispatches on the rule's action params
  target value."
  (:require
   [card-engine.deck.interface :as deck]
   [card-engine.player.interface :as player]
   [card-engine.game.state.interface :as state]))

(defn- deal-to-player
  [game-state [p-idx p] from num-cards]
  (let [draw-pile (get-in game-state [:game/deck-state from])
        {:keys [dealt remaining status]} (deck/deal-cards draw-pile num-cards)
        p' (reduce #(player/add-card %1 %2) p dealt)]
    (->> game-state
         (assoc-in [:game/deck-state from] remaining)
         (assoc-in [:game/deck-state :deck/status] status)
         (assoc-in [:game/players p-idx] p'))))

(defn- deal-to-many-players
  [game-state players from num-cards]
  (let [draw-pile (get-in game-state [:game/deck-state from])]
    (loop [s game-state
           d draw-pile
           [[p-idx p] & ps] players]
      (if (nil? p)
        s
        (let [{:keys [dealt remaining status]} (deck/deal-cards d num-cards)
              p' (reduce #(player/add-card %1 %2) p dealt)]
          (recur
           (-> s
               (assoc-in [:game/deck-state from] remaining)
               (assoc-in [:game/deck-state :deck/status] status)
               (assoc-in [:game/players p-idx] p'))
           remaining
           ps))))))

(defmulti deal-action
  "Applies the deal action to the game state and 
  returns the new game-state. If no dispatcher is found, 
  returns the game-state unchanged.
  
  Args:
  * game-state: The current game state
  * params: The rule's action params
  
  Dispatchers:
  * :current-player - Deals the given number of cards to the current player
  * :all-players - Deals the given number of cards to all players
  * :all-non-dealers - Deals the given number of cards to all non-dealers
  * :dealer - Deals the given number of cards to the dealer"
  (fn [_ params] (:target params)))

(defmethod deal-action :current-player
  [game-state params]
  (let [{:keys [num-cards from]} params
        player (state/current-player game-state)]
    (if player
      (deal-to-player game-state player from num-cards)
      game-state)))

(defmethod deal-action :all-players
  [game-state params]
  (let [{:keys [num-cards from]} params
        players (map #(state/player game-state (player/id %)) (state/players game-state))]
    (deal-to-many-players game-state players from num-cards)))

(defmethod deal-action :all-non-dealers
  [game-state params]
  (let [{:keys [num-cards from]} params
        player (->> (state/players game-state)
                    (filter #(not (player/is-dealer? %)))
                    (map #(state/player game-state (player/id %))))]
    (deal-to-many-players game-state player from num-cards)))

(defmethod deal-action :dealer
  [game-state params]
  (let [{:keys [num-cards from]} params
        dealer (state/dealer game-state)]
    (if dealer
      (deal-to-player game-state dealer from num-cards)
      game-state)))

