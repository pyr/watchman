# watchman

A Clojure library providing a simple facade for Java's [WatchService](http://docs.oracle.com/javase/8/docs/api/java/nio/file/WatchService.html).


## Usage

```clojure
(org.spootnik.watchman/watch!
  "/some/dir"
  (fn [path types] (println types "occured on" path)))

(let [ch (clojure.core.async/chan)]
  (org.spootnik.watchman.async/watch-async! "/some/dir" ch))
```

Both `watch!` and `watch-async!` accept an optional map argument with the following keys:

- `exception!`: callback
- `event-types`: a collection of any keywords from `:create`, `:modify`, `:delete`

Both `watch!` and `watch-async!` return the underlying `WatchService`, `org.spootnik.watchman/close` can
be called on the service.

`org.spootnik.watchman.async/exception-async!` is a utility function which can be called with a channel and
will yield a callback suitable for use as the `:exception!` key in the opts map.

## License

Copyright © 2015 Pierre-Yves Ritschard, MIT License

