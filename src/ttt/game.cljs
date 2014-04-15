(ns ttt.game
  (:require [clojure.string :as string]))

(defn make-board [s] (vec (range (* s s))))

(defn n2d [x y] (+ (* x 3) y))

(defn rotate [b] (vec (flatten
                       (for [x (range 3)] (for [y (range 2 -1 -1)]
                                            (b (n2d y x)))))))

(defn next-player [p] (if (= p \x) \o \x))

(defn play [b p n]
  (if (#{\x \o} (b n)) b (assoc b n p)))

(def lines (conj
            (vec (map #(vec (range %1 (+ %1 3))) (vec (range 0 9 3))))
            (vec (range 0 9 4))))

(defn find-win [b]
  (let [w (first (filter #(apply = (for [i %1] (b i))) lines))]
    (if w
      (b (first w))
      nil)))


(defn game-state [b]
  (let [w (or (find-win b) (find-win (rotate b)))]
    (or w (if (= (count (filter #{\x \o} b)) 9) 0 nil))))

(def new-game {:board (make-board 3) :player \x})

(defn one-move [g m]
  (let [{:keys [board player]} g]
    (let [new-board (play board player m)]
      (if (identical? board new-board)
        g
        {:board new-board :player (next-player player)}))))
