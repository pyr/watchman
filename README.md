watchman: the hero your filesystem needs
========================================

[![Build Status](https://secure.travis-ci.org/pyr/watchman.png)](http://travis-ci.org/pyr/watchman)


A Clojure library providing a simple facade for Java's [WatchService](http://docs.oracle.com/javase/8/docs/api/java/nio/file/WatchService.html).


## Usage

Pull the depenency with leiningen (add this in your `project.clj`)

```clojure
[spootnik/watchman "0.3.5"]
```


```clojure
(watch.man/watch!
  "/some/dir"
  (fn [event] (println (pr-str event)))

(let [ch (clojure.core.async/chan)]
  (watch.man/watch-async! "/some/dir" ch))
```

Both `watch!` and `watch-async!` accept an optional map argument with the following keys:

- `event-types`: a collection of any keywords from `:create`, `:modify`, `:delete`

Both `watch!` and `watch-async!` return the underlying `WatchService`, `watch.man/close` can
be called on the service.

Each argument to the callback when using `watch!` or each payload on the channel
when using `watch-async!` will be a map with the following keys:

- `type`: `:path`, `:exception` or `:closing`
- `path`: The path a `:path` event happens on
- `types`: The types of event a `:path` event was triggered for
- `srv`: The watch service
- `exception`: The exception that was raised for `:exception` events.

## License

Copyright © 2015 Pierre-Yves Ritschard, MIT License

