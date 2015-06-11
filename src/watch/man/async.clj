(ns watch.man.async
  "Async interface for watchman"
  (:require [watch.man          :refer [watch!]]
            [clojure.core.async :refer [put!]]))

(defn watch-async!
  "Async version of watch! which puts events on a channel"
  ([locs ch opts]
     (watch! locs (fn [ev] (put! ch ev)) opts))
  ([locs ch]
     (watch-async! locs ch {})))
