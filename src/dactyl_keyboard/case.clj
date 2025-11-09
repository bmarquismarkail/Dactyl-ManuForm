(ns dactyl-keyboard.case
  (:refer-clojure :exclude [use import])
  (:require [dactyl-keyboard.config :as config]
            [dactyl-keyboard.connectors :as connectors]
            [dactyl-keyboard.keycaps :as keycaps]
            [dactyl-keyboard.layout :as layout]
            [dactyl-keyboard.thumb :as thumb]
            [scad-clj.model :refer :all]
            [scad-clj.scad :refer :all]))

(defn bottom [height p]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (- (/ height 2) 10)])))

(defn bottom-hull [& p]
  (hull p (bottom 0.001 p)))

(def left-wall-x-offset 10)
(def left-wall-z-offset  3)

(defn left-key-position [row direction]
  (map - (layout/key-position 0 row [(* keycaps/mount-width -0.5) (* direction keycaps/mount-height 0.5) 0]) [left-wall-x-offset 0 left-wall-z-offset]))

(defn left-key-place [row direction shape]
  (translate (left-key-position row direction) shape))

(defn wall-locate1 [dx dy] [(* dx config/wall-thickness) (* dy config/wall-thickness) -1])
(defn wall-locate2 [dx dy] [(* dx config/wall-xy-offset) (* dy config/wall-xy-offset) config/wall-z-offset])
(defn wall-locate3 [dx dy] [(* dx (+ config/wall-xy-offset config/wall-thickness)) (* dy (+ config/wall-xy-offset config/wall-thickness)) config/wall-z-offset])

(defn wall-brace [place1 dx1 dy1 post1 place2 dx2 dy2 post2]
  (union
    (hull
      (place1 post1)
      (place1 (translate (wall-locate1 dx1 dy1) post1))
      (place1 (translate (wall-locate2 dx1 dy1) post1))
      (place1 (translate (wall-locate3 dx1 dy1) post1))
      (place2 post2)
      (place2 (translate (wall-locate1 dx2 dy2) post2))
      (place2 (translate (wall-locate2 dx2 dy2) post2))
      (place2 (translate (wall-locate3 dx2 dy2) post2)))
    (bottom-hull
      (place1 (translate (wall-locate2 dx1 dy1) post1))
      (place1 (translate (wall-locate3 dx1 dy1) post1))
      (place2 (translate (wall-locate2 dx2 dy2) post2))
      (place2 (translate (wall-locate3 dx2 dy2) post2)))))

(defn key-wall-brace [x1 y1 dx1 dy1 post1 x2 y2 dx2 dy2 post2]
  (wall-brace (partial layout/key-place x1 y1) dx1 dy1 post1
              (partial layout/key-place x2 y2) dx2 dy2 post2))

(def case-walls
  (union
   ;; back wall
   (for [x (range 0 config/ncols)] (key-wall-brace x 0 0 1 connectors/web-post-tl x       0 0 1 connectors/web-post-tr))
   (for [x (range 1 config/ncols)] (key-wall-brace x 0 0 1 connectors/web-post-tl (dec x) 0 0 1 connectors/web-post-tr))
   (key-wall-brace config/lastcol 0 0 1 connectors/web-post-tr config/lastcol 0 1 0 connectors/web-post-tr)
   ;; right wall
   (for [y (range 0 config/lastrow)] (key-wall-brace config/lastcol y 1 0 connectors/web-post-tr config/lastcol y       1 0 connectors/web-post-br))
   (for [y (range 1 config/lastrow)] (key-wall-brace config/lastcol (dec y) 1 0 connectors/web-post-br config/lastcol y 1 0 connectors/web-post-tr))
   (key-wall-brace config/lastcol config/cornerrow 0 -1 connectors/web-post-br config/lastcol config/cornerrow 1 0 connectors/web-post-br)
   ;; left wall
   (for [y (range 0 config/lastrow)] (union (wall-brace (partial left-key-place y 1)       -1 0 connectors/web-post (partial left-key-place y -1) -1 0 connectors/web-post)
                                     (hull (layout/key-place 0 y connectors/web-post-tl)
                                           (layout/key-place 0 y connectors/web-post-bl)
                                           (left-key-place y  1 connectors/web-post)
                                           (left-key-place y -1 connectors/web-post))))
   (for [y (range 1 config/lastrow)] (union (wall-brace (partial left-key-place (dec y) -1) -1 0 connectors/web-post (partial left-key-place y  1) -1 0 connectors/web-post)
                                     (hull (layout/key-place 0 y       connectors/web-post-tl)
                                           (layout/key-place 0 (dec y) connectors/web-post-bl)
                                           (left-key-place y        1 connectors/web-post)
                                           (left-key-place (dec y) -1 connectors/web-post))))
   (wall-brace (partial layout/key-place 0 0) 0 1 connectors/web-post-tl (partial left-key-place 0 1) 0 1 connectors/web-post)
   (wall-brace (partial left-key-place 0 1) 0 1 connectors/web-post (partial left-key-place 0 1) -1 0 connectors/web-post)
   ;; front wall
   (key-wall-brace config/lastcol 0 0 1 connectors/web-post-tr config/lastcol 0 1 0 connectors/web-post-tr)
   (key-wall-brace 3 config/lastrow   0 -1 connectors/web-post-bl 3 config/lastrow 0.5 -1 connectors/web-post-br)
   (key-wall-brace 3 config/lastrow 0.5 -1 connectors/web-post-br 4 config/cornerrow 1 -1 connectors/web-post-bl)
   (for [x (range 4 config/ncols)] (key-wall-brace x config/cornerrow 0 -1 connectors/web-post-bl x       config/cornerrow 0 -1 connectors/web-post-br))
   (for [x (range 5 config/ncols)] (key-wall-brace x config/cornerrow 0 -1 connectors/web-post-bl (dec x) config/cornerrow 0 -1 connectors/web-post-br))
   ;; thumb walls
   (wall-brace thumb/thumb-mr-place  0 -1 connectors/web-post-br thumb/thumb-tr-place  0 -1 thumb/thumb-post-br)
   (wall-brace thumb/thumb-mr-place  0 -1 connectors/web-post-br thumb/thumb-mr-place  0 -1 connectors/web-post-bl)
   (wall-brace thumb/thumb-br-place  0 -1 connectors/web-post-br thumb/thumb-br-place  0 -1 connectors/web-post-bl)
   (wall-brace thumb/thumb-ml-place -0.3  1 connectors/web-post-tr thumb/thumb-ml-place  0  1 connectors/web-post-tl)
   (wall-brace thumb/thumb-bl-place  0  1 connectors/web-post-tr thumb/thumb-bl-place  0  1 connectors/web-post-tl)
   (wall-brace thumb/thumb-br-place -1  0 connectors/web-post-tl thumb/thumb-br-place -1  0 connectors/web-post-bl)
   (wall-brace thumb/thumb-bl-place -1  0 connectors/web-post-tl thumb/thumb-bl-place -1  0 connectors/web-post-bl)
   ;; thumb corners
   (wall-brace thumb/thumb-br-place -1  0 connectors/web-post-bl thumb/thumb-br-place  0 -1 connectors/web-post-bl)
   (wall-brace thumb/thumb-bl-place -1  0 connectors/web-post-tl thumb/thumb-bl-place  0  1 connectors/web-post-tl)
   ;; thumb tweeners
   (wall-brace thumb/thumb-mr-place  0 -1 connectors/web-post-bl thumb/thumb-br-place  0 -1 connectors/web-post-br)
   (wall-brace thumb/thumb-ml-place  0  1 connectors/web-post-tl thumb/thumb-bl-place  0  1 connectors/web-post-tr)
   (wall-brace thumb/thumb-bl-place -1  0 connectors/web-post-bl thumb/thumb-br-place -1  0 connectors/web-post-tl)
   (wall-brace thumb/thumb-tr-place  0 -1 thumb/thumb-post-br (partial layout/key-place 3 config/lastrow)  0 -1 connectors/web-post-bl)
   ;; clunky bit on the top left thumb connection  (normal connectors don't work well)
   (bottom-hull
     (left-key-place config/cornerrow -1 (translate (wall-locate2 -1 0) connectors/web-post))
     (left-key-place config/cornerrow -1 (translate (wall-locate3 -1 0) connectors/web-post))
     (thumb/thumb-ml-place (translate (wall-locate2 -0.3 1) connectors/web-post-tr))
     (thumb/thumb-ml-place (translate (wall-locate3 -0.3 1) connectors/web-post-tr)))
   (hull
     (left-key-place config/cornerrow -1 (translate (wall-locate2 -1 0) connectors/web-post))
     (left-key-place config/cornerrow -1 (translate (wall-locate3 -1 0) connectors/web-post))
     (thumb/thumb-ml-place (translate (wall-locate2 -0.3 1) connectors/web-post-tr))
     (thumb/thumb-ml-place (translate (wall-locate3 -0.3 1) connectors/web-post-tr))
     (thumb/thumb-tl-place thumb/thumb-post-tl))
   (hull
     (left-key-place config/cornerrow -1 connectors/web-post)
     (left-key-place config/cornerrow -1 (translate (wall-locate1 -1 0) connectors/web-post))
     (left-key-place config/cornerrow -1 (translate (wall-locate2 -1 0) connectors/web-post))
     (left-key-place config/cornerrow -1 (translate (wall-locate3 -1 0) connectors/web-post))
     (thumb/thumb-tl-place thumb/thumb-post-tl))
   (hull
     (left-key-place config/cornerrow -1 connectors/web-post)
     (left-key-place config/cornerrow -1 (translate (wall-locate1 -1 0) connectors/web-post))
     (layout/key-place 0 config/cornerrow connectors/web-post-bl)
     (layout/key-place 0 config/cornerrow (translate (wall-locate1 -1 0) connectors/web-post-bl))
     (thumb/thumb-tl-place thumb/thumb-post-tl))
   (hull
     (thumb/thumb-ml-place connectors/web-post-tr)
     (thumb/thumb-ml-place (translate (wall-locate1 -0.3 1) connectors/web-post-tr))
     (thumb/thumb-ml-place (translate (wall-locate2 -0.3 1) connectors/web-post-tr))
     (thumb/thumb-ml-place (translate (wall-locate3 -0.3 1) connectors/web-post-tr))
     (thumb/thumb-tl-place thumb/thumb-post-tl))))
