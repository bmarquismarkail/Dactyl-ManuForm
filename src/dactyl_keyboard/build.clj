(ns dactyl-keyboard.build
  (:refer-clojure :exclude [use import])
  (:require [dactyl-keyboard.case :as case]
            [dactyl-keyboard.connectors :as connectors]
            [dactyl-keyboard.electronics :as electronics]
            [dactyl-keyboard.layout :as layout]
            [dactyl-keyboard.thumb :as thumb]
            [scad-clj.model :refer :all]
            [scad-clj.scad :refer :all]))

(def model-right (difference
                   (union
                    layout/key-holes
                    connectors/connectors
                    thumb/thumb
                    thumb/thumb-connectors
                    (difference (union case/case-walls
                                       electronics/screw-insert-outers
                                       electronics/teensy-holder
                                       electronics/usb-holder)
                                electronics/rj9-space
                                electronics/usb-holder-hole
                                electronics/screw-insert-holes)
                    electronics/rj9-holder
                    electronics/wire-posts)
                   (translate [0 0 -20] (cube 350 350 40))))

(spit "things/right.scad"
      (write-scad model-right))

(spit "things/left.scad"
      (write-scad (mirror [-1 0 0] model-right)))

(spit "things/right-test.scad"
      (write-scad
                   (union
                    layout/key-holes
                    connectors/connectors
                    thumb/thumb
                    thumb/thumb-connectors
                    case/case-walls
                    thumb/thumbcaps
                    layout/caps
                    electronics/teensy-holder
                    electronics/rj9-holder
                    electronics/usb-holder-hole)))

(spit "things/right-plate.scad"
      (write-scad
                   (cut
                     (translate [0 0 -0.1]
                       (difference (union case/case-walls
                                          electronics/teensy-holder
                                          electronics/screw-insert-outers)
                                   (translate [0 0 -10] electronics/screw-insert-screw-holes)))))

(spit "things/test.scad"
      (write-scad
         (difference electronics/usb-holder electronics/usb-holder-hole)))

(defn -main [dum] 1)
