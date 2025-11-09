(ns dactyl-keyboard.thumb
  (:refer-clojure :exclude [use import])
  (:require [dactyl-keyboard.config :as config]
            [dactyl-keyboard.connectors :as connectors]
            [dactyl-keyboard.keycaps :as keycaps]
            [dactyl-keyboard.layout :as layout]
            [scad-clj.model :refer :all]
            [scad-clj.scad :refer :all]
            [unicode-math.core :refer :all]))

(def thumborigin
  (map + (layout/key-position 1 config/cornerrow [(/ keycaps/mount-width 2) (- (/ keycaps/mount-height 2)) 0])
         config/thumb-offsets))

(defn thumb-tr-place [shape]
  (->> shape
       (rotate (config/deg2rad  10) [1 0 0])
       (rotate (config/deg2rad -23) [0 1 0])
       (rotate (config/deg2rad  10) [0 0 1])
       (translate thumborigin)
       (translate [-12 -16 3])))

(defn thumb-tl-place [shape]
  (->> shape
       (rotate (config/deg2rad  10) [1 0 0])
       (rotate (config/deg2rad -23) [0 1 0])
       (rotate (config/deg2rad  10) [0 0 1])
       (translate thumborigin)
       (translate [-32 -15 -2])))

(defn thumb-mr-place [shape]
  (->> shape
       (rotate (config/deg2rad  -6) [1 0 0])
       (rotate (config/deg2rad -34) [0 1 0])
       (rotate (config/deg2rad  48) [0 0 1])
       (translate thumborigin)
       (translate [-29 -40 -13])))

(defn thumb-ml-place [shape]
  (->> shape
       (rotate (config/deg2rad   6) [1 0 0])
       (rotate (config/deg2rad -34) [0 1 0])
       (rotate (config/deg2rad  40) [0 0 1])
       (translate thumborigin)
       (translate [-51 -25 -12])))

(defn thumb-br-place [shape]
  (->> shape
       (rotate (config/deg2rad -16) [1 0 0])
       (rotate (config/deg2rad -33) [0 1 0])
       (rotate (config/deg2rad  54) [0 0 1])
       (translate thumborigin)
       (translate [-37.8 -55.3 -25.3])))

(defn thumb-bl-place [shape]
  (->> shape
       (rotate (config/deg2rad  -4) [1 0 0])
       (rotate (config/deg2rad -35) [0 1 0])
       (rotate (config/deg2rad  52) [0 0 1])
       (translate thumborigin)
       (translate [-56.3 -43.3 -23.5])))

(defn thumb-1x-layout [shape]
  (union
   (thumb-mr-place shape)
   (thumb-ml-place shape)
   (thumb-br-place shape)
   (thumb-bl-place shape)))

(defn thumb-15x-layout [shape]
  (union
   (thumb-tr-place shape)
   (thumb-tl-place shape)))

(def larger-plate
  (let [plate-height (/ (- keycaps/sa-double-length keycaps/mount-height) 3)
        top-plate (->> (cube keycaps/mount-width plate-height connectors/web-thickness)
                       (translate [0 (/ (+ plate-height keycaps/mount-height) 2)
                                   (- keycaps/plate-thickness (/ connectors/web-thickness 2))]))]
    (union top-plate (mirror [0 1 0] top-plate))))

(def thumbcaps
  (union
   (thumb-1x-layout (keycaps/sa-cap 1))
   (thumb-15x-layout (rotate (/ π 2) [0 0 1] (keycaps/sa-cap 1.5)))))

(def thumb
  (union
   (thumb-1x-layout keycaps/single-plate)
   (thumb-15x-layout keycaps/single-plate)
   (thumb-15x-layout larger-plate)))

(def thumb-post-tr (translate [(- (/ keycaps/mount-width 2) connectors/post-adj)  (- (/ keycaps/mount-height  1.15) connectors/post-adj) 0] connectors/web-post))
(def thumb-post-tl (translate [(+ (/ keycaps/mount-width -2) connectors/post-adj) (- (/ keycaps/mount-height  1.15) connectors/post-adj) 0] connectors/web-post))
(def thumb-post-bl (translate [(+ (/ keycaps/mount-width -2) connectors/post-adj) (+ (/ keycaps/mount-height -1.15) connectors/post-adj) 0] connectors/web-post))
(def thumb-post-br (translate [(- (/ keycaps/mount-width 2) connectors/post-adj)  (+ (/ keycaps/mount-height -1.15) connectors/post-adj) 0] connectors/web-post))

(def thumb-connectors
  (union
      (connectors/triangle-hulls    ; top two
             (thumb-tl-place thumb-post-tr)
             (thumb-tl-place thumb-post-br)
             (thumb-tr-place thumb-post-tl)
             (thumb-tr-place thumb-post-bl))
      (connectors/triangle-hulls    ; bottom two on the right
             (thumb-br-place connectors/web-post-tr)
             (thumb-br-place connectors/web-post-br)
             (thumb-mr-place connectors/web-post-tl)
             (thumb-mr-place connectors/web-post-bl))
      (connectors/triangle-hulls    ; bottom two on the left
             (thumb-bl-place connectors/web-post-tr)
             (thumb-bl-place connectors/web-post-br)
             (thumb-ml-place connectors/web-post-tl)
             (thumb-ml-place connectors/web-post-bl))
      (connectors/triangle-hulls    ; centers of the bottom four
             (thumb-br-place connectors/web-post-tl)
             (thumb-bl-place connectors/web-post-bl)
             (thumb-br-place connectors/web-post-tr)
             (thumb-bl-place connectors/web-post-br)
             (thumb-mr-place connectors/web-post-tl)
             (thumb-ml-place connectors/web-post-bl)
             (thumb-mr-place connectors/web-post-tr)
             (thumb-ml-place connectors/web-post-br))
      (connectors/triangle-hulls    ; top two to the middle two, starting on the left
             (thumb-tl-place thumb-post-tl)
             (thumb-ml-place connectors/web-post-tr)
             (thumb-tl-place thumb-post-bl)
             (thumb-ml-place connectors/web-post-br)
             (thumb-tl-place thumb-post-br)
             (thumb-mr-place connectors/web-post-tr)
             (thumb-tr-place thumb-post-bl)
             (thumb-mr-place connectors/web-post-br)
             (thumb-tr-place thumb-post-br))
      (connectors/triangle-hulls    ; top two to the main keyboard, starting on the left
             (thumb-tl-place thumb-post-tl)
             (layout/key-place 0 config/cornerrow connectors/web-post-bl)
             (thumb-tl-place thumb-post-tr)
             (layout/key-place 0 config/cornerrow connectors/web-post-br)
             (thumb-tr-place thumb-post-tl)
             (layout/key-place 1 config/cornerrow connectors/web-post-bl)
             (thumb-tr-place thumb-post-tr)
             (layout/key-place 1 config/cornerrow connectors/web-post-br)
             (layout/key-place 2 config/lastrow connectors/web-post-tl)
             (layout/key-place 2 config/lastrow connectors/web-post-bl)
             (thumb-tr-place thumb-post-tr)
             (layout/key-place 2 config/lastrow connectors/web-post-bl)
             (thumb-tr-place thumb-post-br)
             (layout/key-place 2 config/lastrow connectors/web-post-br)
             (layout/key-place 3 config/lastrow connectors/web-post-bl)
             (layout/key-place 2 config/lastrow connectors/web-post-tr)
             (layout/key-place 3 config/lastrow connectors/web-post-tl)
             (layout/key-place 3 config/cornerrow connectors/web-post-bl)
             (layout/key-place 3 config/lastrow connectors/web-post-tr)
             (layout/key-place 3 config/lastrow connectors/web-post-br)
             (layout/key-place 4 config/cornerrow connectors/web-post-bl))))
