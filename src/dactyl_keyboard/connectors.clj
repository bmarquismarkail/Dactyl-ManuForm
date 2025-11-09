(ns dactyl-keyboard.connectors
  (:refer-clojure :exclude [use import])
  (:require [dactyl-keyboard.config :as config]
            [dactyl-keyboard.keycaps :as keycaps]
            [dactyl-keyboard.layout :as layout]
            [scad-clj.model :refer :all]
            [scad-clj.scad :refer :all]))

(def web-thickness 3.5)
(def post-size 0.1)
(def web-post (->> (cube post-size post-size web-thickness)
                   (translate [0 0 (+ (/ web-thickness -2)
                                      keycaps/plate-thickness)])))

(def post-adj (/ post-size 2))
(def web-post-tr (translate [(- (/ keycaps/mount-width 2) post-adj) (- (/ keycaps/mount-height 2) post-adj) 0] web-post))
(def web-post-tl (translate [(+ (/ keycaps/mount-width -2) post-adj) (- (/ keycaps/mount-height 2) post-adj) 0] web-post))
(def web-post-bl (translate [(+ (/ keycaps/mount-width -2) post-adj) (+ (/ keycaps/mount-height -2) post-adj) 0] web-post))
(def web-post-br (translate [(- (/ keycaps/mount-width 2) post-adj) (+ (/ keycaps/mount-height -2) post-adj) 0] web-post))

(defn triangle-hulls [& shapes]
  (apply union
         (map (partial apply hull)
              (partition 3 1 shapes))))

(def connectors
  (apply union
         (concat
          ;; Row connections
          (for [column (range 0 (dec config/ncols))
                row (range 0 config/lastrow)]
            (triangle-hulls
             (layout/key-place (inc column) row web-post-tl)
             (layout/key-place column row web-post-tr)
             (layout/key-place (inc column) row web-post-bl)
             (layout/key-place column row web-post-br)))

          ;; Column connections
          (for [column config/columns
                row (range 0 config/cornerrow)]
            (triangle-hulls
             (layout/key-place column row web-post-bl)
             (layout/key-place column row web-post-br)
             (layout/key-place column (inc row) web-post-tl)
             (layout/key-place column (inc row) web-post-tr)))

          ;; Diagonal connections
          (for [column (range 0 (dec config/ncols))
                row (range 0 config/cornerrow)]
            (triangle-hulls
             (layout/key-place column row web-post-br)
             (layout/key-place column (inc row) web-post-tr)
             (layout/key-place (inc column) row web-post-bl)
             (layout/key-place (inc column) (inc row) web-post-tl))))))
