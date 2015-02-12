(ns org.spootnik.watchman
  "Small facade for Java's WatchService to be notified of FS changes"
  (:import java.nio.file.WatchService
           java.nio.file.StandardWatchEventKinds
           java.nio.file.FileSystems))

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
  [fs srv types location]
  (-> (.getPath fs location (make-array String 0))
      (.register srv types)))

(defn group-events
  "When receiving multiple events on a key, group them by path"
  [polled]
  (let [watch-events (seq polled)]
    (->> (for [event watch-events
               :let [kind  (evt->kw (.kind event))]
               :when (not= kind :overflow)]
           [(.context event) kind])
         (group-by first)
         (map (juxt key (comp (partial map second) val)))
         (reduce merge {}))))

(defn close
  "Close a watch service"
  [service]
  (.close service))

(defn watch!
  "Watch a location or seq of locations and call notify! with two
   args (path and types) when events occur. Accepts an optional map
   of options: exception! is a callback and event-types a list of keywords"
  ([locations notify! {:keys [event-types exception!]
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
                   (notify! path types))
                 (recur (if (.reset k) keys (remove (partial = k) keys))))))
           (catch Exception e
             (when exception!
               (exception! srv e)))
           (finally
             (.close srv))))
       srv))
  ([locations notify!]
     (watch! locations notify! {})))
