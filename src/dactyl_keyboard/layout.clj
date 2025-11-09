(ns dactyl-keyboard.layout
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [mmul]]
            [dactyl-keyboard.config :as config]
            [dactyl-keyboard.keycaps :as keycaps]
            [scad-clj.model :refer :all]
            [scad-clj.scad :refer :all]))

(def cap-top-height (+ keycaps/plate-thickness keycaps/sa-profile-key-height))
(def row-radius (+ (/ (/ (+ keycaps/mount-height config/extra-height) 2)
                      (Math/sin (/ config/α 2)))
                   cap-top-height))
(def column-radius (+ (/ (/ (+ keycaps/mount-width config/extra-width) 2)
                         (Math/sin (/ config/β 2)))
                      cap-top-height))
(def column-x-delta (+ -1 (- (* column-radius (Math/sin config/β)))))
(def column-base-angle (* config/β (- config/centercol 2)))

(defn apply-key-geometry [translate-fn rotate-x-fn rotate-y-fn column row shape]
  (let [column-angle (* config/β (- config/centercol column))
        placed-shape (->> shape
                          (translate-fn [0 0 (- row-radius)])
                          (rotate-x-fn  (* config/α (- config/centerrow row)))
                          (translate-fn [0 0 row-radius])
                          (translate-fn [0 0 (- column-radius)])
                          (rotate-y-fn  column-angle)
                          (translate-fn [0 0 column-radius])
                          (translate-fn (config/column-offset column)))
        column-z-delta (* column-radius (- 1 (Math/cos column-angle)))
        placed-shape-ortho (->> shape
                                (translate-fn [0 0 (- row-radius)])
                                (rotate-x-fn  (* config/α (- config/centerrow row)))
                                (translate-fn [0 0 row-radius])
                                (rotate-y-fn  column-angle)
                                (translate-fn [(- (* (- column config/centercol) column-x-delta)) 0 column-z-delta])
                                (translate-fn (config/column-offset column)))
        placed-shape-fixed (->> shape
                                (rotate-y-fn  (nth config/fixed-angles column))
                                (translate-fn [(nth config/fixed-x column) 0 (nth config/fixed-z column)])
                                (translate-fn [0 0 (- (+ row-radius (nth config/fixed-z column)))])
                                (rotate-x-fn  (* config/α (- config/centerrow row)))
                                (translate-fn [0 0 (+ row-radius (nth config/fixed-z column))])
                                (rotate-y-fn  config/fixed-tenting)
                                (translate-fn [0 (second (config/column-offset column)) 0]))]
    (->> (case config/column-style
          :orthographic placed-shape-ortho
          :fixed        placed-shape-fixed
                        placed-shape)
         (rotate-y-fn  config/tenting-angle)
         (translate-fn [0 0 config/keyboard-z-offset]))))

(defn key-place [column row shape]
  (apply-key-geometry translate
    (fn [angle obj] (rotate angle [1 0 0] obj))
    (fn [angle obj] (rotate angle [0 1 0] obj))
    column row shape))

(defn rotate-around-x [angle position]
  (mmul
   [[1 0 0]
    [0 (Math/cos angle) (- (Math/sin angle))]
    [0 (Math/sin angle)    (Math/cos angle)]]
   position))

(defn rotate-around-y [angle position]
  (mmul
   [[(Math/cos angle)     0 (Math/sin angle)]
    [0                    1 0]
    [(- (Math/sin angle)) 0 (Math/cos angle)]]
   position))

(defn key-position [column row position]
  (apply-key-geometry (partial map +) rotate-around-x rotate-around-y column row position))

(def key-holes
  (apply union
         (for [column config/columns
               row config/rows
               :when (or (.contains [2 3] column)
                         (not= row config/lastrow))]
           (->> keycaps/single-plate
                (key-place column row)))))

(def caps
  (apply union
         (for [column config/columns
               row config/rows
               :when (or (.contains [2 3] column)
                         (not= row config/lastrow))]
           (->> (keycaps/sa-cap (if (= column 5) 1 1))
                (key-place column row)))))
