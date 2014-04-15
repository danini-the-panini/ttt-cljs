(ns ttt.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [clojure.data :as data]
            [clojure.string :as string]
            [ttt.game :as game]))


(enable-console-print!)

(defn fresh-state [] [game/new-game])

(def app-state
  (atom
   {:title "Tic-Tac-Toe!"
    :game-states (fresh-state)}))


(defn undo-game [cursor]
  (when (> (count (:game-states @app-state)) 1)
    (om/transact! cursor :game-states pop)))

(defn make-move [cursor i]
  (let [state (last (:game-states @app-state))]
    (if (game/game-state (:board state))
      (om/transact! cursor :game-states fresh-state)
      (let [new-state (game/one-move state i)]
        (when (not(identical? state new-state))
          (om/transact! cursor :game-states #(conj % new-state)))))))

(defn board-view [cursor owner]
  (reify
    om/IRender
    (render [this]
            (let [board (:board (last (:game-states cursor)))]
              (apply dom/div #js {:className "board"}
                     (map-indexed #(dom/div #js{:id %1 :className "cell"
                                                :onClick (fn [e] (make-move cursor %1))}
                                            (or (#{\x \o} %2) " "))
                                  board))))))

(defn game-state-view [cursor owner]
  (reify
    om/IRenderState
    (render-state [this state]
                  (let [game-state (last (:game-states cursor))]
                    (let [{:keys [player board]} game-state]
                      (dom/div nil
                               (let [state (game/game-state board)]
                                 (dom/h2 nil
                                         (if state
                                           (if (zero? state)
                                             "It's a Draw!"
                                             (str state " wins!!"))
                                           (str player " to play..."))))
                               (om/build board-view cursor)
                               (dom/button #js {:onClick #(undo-game cursor)} "Undo")))))))

(om/root
 (fn [app owner]
   (dom/div nil
            (dom/h1 nil (:title app))
            (om/build game-state-view app)))
 app-state
 {:target (. js/document (getElementById "app"))})
