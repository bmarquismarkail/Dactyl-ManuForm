
(defproject dactyl-keyboard "0.1.0-SNAPSHOT"
  :description "A parameterized, split-hand, concave, columnar, ergonomic keyboard."
  :url "https://github.com/tshort/dactyl-keyboard"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main dactyl-keyboard.dactyl
  :source-paths ["src"]
  :test-paths ["test"]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [unicode-math "0.2.0"]
                 [scad-clj "0.4.0"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.1"]]}})


