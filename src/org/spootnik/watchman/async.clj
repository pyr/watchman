(ns org.spootnik.watchman.async
  "Async interface for watchman"
  (:require [org.spootnik.watchman :refer [watch!]]
            [clojure.core.async    :refer [put!]]))

(defn exception-async!
  "Simplistic exception callback which puts on a chan"
  [ch]
  (fn [srv e]
    (put! ch {:service srv :exception e})))

(defn watch-async!
  "Async version of watch! which puts events on a channel"
  ([locs ch opts]
     (watch! locs (fn [path types] (put! ch {:path path :types types})) opts))
  ([locs ch]
     (watch-async! locs ch {})))
