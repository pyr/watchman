(defproject spootnik/watchman "0.3.8-SNAPSHOT"
  :description "Watch directories for changes"
  :url "https://github.com/pyr/watchman"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :global-vars {*warn-on-reflection* true}
  :plugins [[lein-ancient "0.6.15"]]
  :dependencies [[org.clojure/clojure "1.10.1"]])
