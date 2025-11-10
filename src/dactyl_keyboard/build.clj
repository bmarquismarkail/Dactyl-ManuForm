(ns dactyl-keyboard.build
  (:refer-clojure :exclude [use import])
  (:require [clojure.java.io :as io]
            [dactyl-keyboard.case :as case]
            [dactyl-keyboard.connectors :as connectors]
            [dactyl-keyboard.electronics :as electronics]
            [dactyl-keyboard.layout :as layout]
            [dactyl-keyboard.thumb :as thumb]
            [scad-clj.model :refer :all]
            [scad-clj.scad :refer :all]))

(def model-right
  (difference
   (union
    layout/key-holes
    connectors/connectors
    thumb/thumb
    thumb/thumb-connectors
    (difference
     (union case/case-walls
            electronics/screw-insert-outers
            electronics/teensy-holder
            electronics/usb-holder)
     electronics/rj9-space
     electronics/usb-holder-hole
     electronics/screw-insert-holes)
    electronics/rj9-holder
    electronics/wire-posts)
   (translate [0 0 -20] (cube 350 350 40))))

(def model-left
  (mirror [-1 0 0] model-right))

(def right-test
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
   electronics/usb-holder-hole))

(def right-plate
  (cut
   (translate
    [0 0 -0.1]
    (difference
     (union case/case-walls
            electronics/teensy-holder
            electronics/screw-insert-outers)
     (translate [0 0 -10] electronics/screw-insert-screw-holes)))))

(def usb-test
  (difference electronics/usb-holder electronics/usb-holder-hole))

(def outputs
  {"things/right.scad"       model-right
   "things/left.scad"        model-left
   "things/right-test.scad"  right-test
   "things/right-plate.scad" right-plate
   "things/test.scad"        usb-test})

(defn- write-output! [path shape]
  (io/make-parents path)
  (spit path (write-scad shape)))

(defn generate-models!
  "Write all SCAD outputs to the things/ directory."
  []
  (doseq [[path shape] outputs]
    (write-output! path shape))
  (println "Generated" (count outputs) "SCAD files in things/"))

(defn -main
  "Entry point for Leiningen runs."
  [& _]
  (generate-models!))
