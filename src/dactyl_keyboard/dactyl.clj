(ns dactyl-keyboard.dactyl
  (:refer-clojure :exclude [use import])
  (:require [dactyl-keyboard.build :as build :refer [model-right generate-models!]]
            [dactyl-keyboard.case :as case :refer :all]
            [dactyl-keyboard.config :as config :refer :all]
            [dactyl-keyboard.connectors :as connectors :refer :all]
            [dactyl-keyboard.electronics :as electronics :refer :all]
            [dactyl-keyboard.keycaps :as keycaps :refer :all]
            [dactyl-keyboard.layout :as layout :refer :all]
            [dactyl-keyboard.thumb :as thumb :refer :all]))

(defn -main
  [& args]
  (apply build/-main args))
