(ns dactyl-keyboard.electronics
  (:refer-clojure :exclude [use import])
  (:require [dactyl-keyboard.case :as case]
            [dactyl-keyboard.config :as config]
            [dactyl-keyboard.keycaps :as keycaps]
            [dactyl-keyboard.layout :as layout]
            [dactyl-keyboard.thumb :as thumb]
            [scad-clj.model :refer :all]
            [scad-clj.scad :refer :all]))

(def rj9-start  (map + [0 -3  0] (layout/key-position 0 0 (map + (case/wall-locate3 0 1) [0 (/ keycaps/mount-height  2) 0]))))
(def rj9-position  [(first rj9-start) (second rj9-start) 11])
(def rj9-cube   (cube 14.78 13 22.38))
(def rj9-space  (translate rj9-position rj9-cube))
(def rj9-holder (translate rj9-position
                  (difference rj9-cube
                              (union (translate [0 2 0] (cube 10.78  9 18.38))
                                     (translate [0 0 5] (cube 10.78 13  5))))))

(def usb-holder-position (layout/key-position 1 0 (map + (case/wall-locate2 0 1) [0 (/ keycaps/mount-height 2) 0])))
(def usb-holder-size [6.5 10.0 13.6])
(def usb-holder-thickness 4)
(def usb-holder
    (->> (cube (+ (first usb-holder-size) usb-holder-thickness) (second usb-holder-size) (+ (last usb-holder-size) usb-holder-thickness))
         (translate [(first usb-holder-position) (second usb-holder-position) (/ (+ (last usb-holder-size) usb-holder-thickness) 2)])))
(def usb-holder-hole
    (->> (apply cube usb-holder-size)
         (translate [(first usb-holder-position) (second usb-holder-position) (/ (+ (last usb-holder-size) usb-holder-thickness) 2)])))

(def teensy-width 20)
(def teensy-height 12)
(def teensy-length 33)
(def teensy2-length 53)
(def teensy-pcb-thickness 2)
(def teensy-holder-width  (+ 7 teensy-pcb-thickness))
(def teensy-holder-height (+ 6 teensy-width))
(def teensy-offset-height 5)
(def teensy-holder-top-length 18)
(def teensy-top-xy (layout/key-position 0 (- config/centerrow 1) (case/wall-locate3 -1 0)))
(def teensy-bot-xy (layout/key-position 0 (+ config/centerrow 1) (case/wall-locate3 -1 0)))
(def teensy-holder-length (- (second teensy-top-xy) (second teensy-bot-xy)))
(def teensy-holder-offset (/ teensy-holder-length -2))
(def teensy-holder-top-offset (- (/ teensy-holder-top-length 2) teensy-holder-length))

(def teensy-holder
    (->>
        (union
          (->> (cube 3 teensy-holder-length (+ 6 teensy-width))
               (translate [1.5 teensy-holder-offset 0]))
          (->> (cube teensy-pcb-thickness teensy-holder-length 3)
               (translate [(+ (/ teensy-pcb-thickness 2) 3) teensy-holder-offset (- -1.5 (/ teensy-width 2))]))
          (->> (cube 4 teensy-holder-length 4)
               (translate [(+ teensy-pcb-thickness 5) teensy-holder-offset (-  -1 (/ teensy-width 2))]))
          (->> (cube teensy-pcb-thickness teensy-holder-top-length 3)
               (translate [(+ (/ teensy-pcb-thickness 2) 3) teensy-holder-top-offset (+ 1.5 (/ teensy-width 2))]))
          (->> (cube 4 teensy-holder-top-length 4)
               (translate [(+ teensy-pcb-thickness 5) teensy-holder-top-offset (+ 1 (/ teensy-width 2))])))
        (translate [(- teensy-holder-width) 0 0])
        (translate [-1.4 0 0])
        (translate [(first teensy-top-xy)
                    (- (second teensy-top-xy) 1)
                    (/ (+ 6 teensy-width) 2)])))

(defn screw-insert-shape [bottom-radius top-radius height]
   (union (cylinder [bottom-radius top-radius] height)
          (translate [0 0 (/ height 2)] (sphere top-radius))))

(defn screw-insert [column row bottom-radius top-radius height]
  (let [shift-right   (= column config/lastcol)
        shift-left    (= column 0)
        shift-up      (and (not (or shift-right shift-left)) (= row 0))
        shift-down    (and (not (or shift-right shift-left)) (>= row config/lastrow))
        position      (if shift-up     (layout/key-position column row (map + (case/wall-locate2  0  1) [0 (/ keycaps/mount-height 2) 0]))
                       (if shift-down  (layout/key-position column row (map - (case/wall-locate2  0 -1) [0 (/ keycaps/mount-height 2) 0]))
                        (if shift-left (map + (case/left-key-position row 0) (case/wall-locate3 -1 0))
                                       (layout/key-position column row (map + (case/wall-locate2  1  0) [(/ keycaps/mount-width 2) 0 0])))))
        ]
    (->> (screw-insert-shape bottom-radius top-radius height)
         (translate [(first position) (second position) (/ height 2)])
    )))

(defn screw-insert-all-shapes [bottom-radius top-radius height]
  (union (screw-insert 0 0         bottom-radius top-radius height)
         (screw-insert 0 config/lastrow   bottom-radius top-radius height)
         (screw-insert 2 (+ config/lastrow 0.3)  bottom-radius top-radius height)
         (screw-insert 3 0         bottom-radius top-radius height)
         (screw-insert config/lastcol 1   bottom-radius top-radius height)))
(def screw-insert-height 3.8)
(def screw-insert-bottom-radius (/ 5.31 2))
(def screw-insert-top-radius (/ 5.1 2))
(def screw-insert-holes  (screw-insert-all-shapes screw-insert-bottom-radius screw-insert-top-radius screw-insert-height))
(def screw-insert-outers (screw-insert-all-shapes (+ screw-insert-bottom-radius 1.6) (+ screw-insert-top-radius 1.6) (+ screw-insert-height 1.5)))
(def screw-insert-screw-holes  (screw-insert-all-shapes 1.7 1.7 350))

(def wire-post-height 7)
(def wire-post-overhang 3.5)
(def wire-post-diameter 2.6)
(defn wire-post [direction offset]
   (->> (union (translate [0 (* wire-post-diameter -0.5 direction) 0] (cube wire-post-diameter wire-post-diameter wire-post-height))
               (translate [0 (* wire-post-overhang -0.5 direction) (/ wire-post-height -2)] (cube wire-post-diameter wire-post-overhang wire-post-diameter)))
        (translate [0 (- offset) (+ (/ wire-post-height -2) 3) ])
        (rotate (/ config/α -2) [1 0 0])
        (translate [3 (/ keycaps/mount-height -2) 0])))

(def wire-posts
  (union
     (thumb/thumb-ml-place (translate [-5 0 -2] (wire-post  1 0)))
     (thumb/thumb-ml-place (translate [ 0 0 -2.5] (wire-post -1 6)))
     (thumb/thumb-ml-place (translate [ 5 0 -2] (wire-post  1 0)))
     (for [column (range 0 config/lastcol)
           row (range 0 config/cornerrow)]
       (union
        (layout/key-place column row (translate [-5 0 0] (wire-post 1 0)))
        (layout/key-place column row (translate [0 0 0] (wire-post -1 6)))
        (layout/key-place column row (translate [5 0 0] (wire-post  1 0)))))))
