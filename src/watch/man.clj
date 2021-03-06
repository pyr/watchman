(ns watch.man
  "Small facade for Java's WatchService to be notified of FS changes"
  (:import java.nio.file.WatchService
           java.nio.file.WatchEvent
           java.nio.file.StandardWatchEventKinds
           java.nio.file.FileSystems
           java.nio.file.FileSystem))

;; convenience functions

(def evt->kw
  "Map WatchEventKinds to keywords"
  {StandardWatchEventKinds/ENTRY_CREATE :create
   StandardWatchEventKinds/ENTRY_DELETE :delete
   StandardWatchEventKinds/ENTRY_MODIFY :modify
   StandardWatchEventKinds/OVERFLOW     :overflow})

(def kw->evt
  "Map keywords to WatchEventKinds"
  (->> evt->kw
       (map (comp vec reverse))
       (reduce merge {})))

(defn ->evts
  "Prepare a list of event types for Path/register"
  [coll]
  (into-array
   (class StandardWatchEventKinds/ENTRY_CREATE)
   (map kw->evt (if (sequential? coll) coll [coll]))))

(defn ->key
  "Create a watch key"
  [^FileSystem fs srv types ^String location]
  (-> (.getPath fs location (make-array String 0))
      (.register srv types)))

(defn group-events
  "When receiving multiple events on a key, group them by path"
  [polled]
  (let [watch-events (seq polled)]
    (->> (for [^WatchEvent event watch-events
               :let [kind  (evt->kw (.kind event))]
               :when (not= kind :overflow)]
           [(.context event) kind])
         (group-by first)
         (map (juxt key (comp (partial map second) val)))
         (reduce merge {}))))

;; "Public" api

(defn close
  "Close a watch service"
  [^WatchService service]
  (.close service))

(defn ->path
  "Construct a path from "
  ([base elems]
     (.getPath
      (FileSystems/getDefault)
      base
      (into-array String (map str (if (sequential? elems) elems [elems])))))
  ([base elems & more]
     (->path base conj (list more) elems)))

(defn watch!
  "Watch a location or seq of locations and call notify! with two
   args (path and types) when events occur. Accepts an optional map
   of options: exception! is a callback and event-types a list of keywords"
  ([locations notify! {:keys [event-types]
                       :or {event-types [:create :modify :delete]}}]
     (let [fs    (FileSystems/getDefault)
           srv   (.newWatchService fs)
           locs  (if (sequential? locations) locations [locations])
           keys  (mapv (partial ->key fs srv (->evts event-types)) locs)]
       (future
         (try
           (loop [keys keys]
             (when (seq keys)
               (let [k (.take srv)]
                 (doseq [[path types] (group-events (.pollEvents k))]
                   (notify! {:type :path :path path :types types :srv srv}))
                 (recur (if (.reset k) keys (remove (partial = k) keys))))))
           (catch Exception e
             (notify! {:type :exception :srv srv :exception e}))
           (finally
             (.close srv)
             (notify! {:type :closing :srv srv}))))
       srv))
  ([locations notify!]
     (watch! locations notify! {})))
