(ns com.wsscode.transito
  (:refer-clojure :exclude [read write])
  (:require
    [cognitect.transit :as t]
    #?(:cljs [goog.object :as gobj]))
  #?(:clj
     (:import
       (cognitect.transit
         HandlerMapContainer)
       (com.cognitect.transit
         TransitFactory)
       (java.io
         ByteArrayInputStream
         ByteArrayOutputStream
         OutputStream)
       (java.util.function
         Function))))

(def unknown-default-handler
  (t/write-handler (fn [_] "unknown") #(pr-str %)))

(defn read-str
  ([s] (read-str s {}))
  ([s options]
   #?(:clj
      (let [in     (ByteArrayInputStream. (.getBytes s "UTF-8"))
            reader (t/reader in :json options)]
        (t/read reader))

      :cljs
      (let [reader (t/reader :json options)]
        (t/read reader s)))))

#?(:cljs
   (def cljs-write-handlers
     {"default" unknown-default-handler}))

#?(:clj
   (defn writer
     "Creates a writer over the provided destination `out` using
      the specified format, one of: :msgpack, :json or :json-verbose.
      An optional opts map may be passed. Supported options are:
      :handlers - a map of types to WriteHandler instances, they are merged
      with the default-handlers and then with the default handlers
      provided by transit-java.
      :transform - a function of one argument that will transform values before
      they are written."
     ([out type] (writer out type {}))
     ([^OutputStream out type {:keys [handlers default-handler transform]}]
      (if (#{:json :json-verbose :msgpack} type)
        (let [handler-map (if (instance? HandlerMapContainer handlers)
                            (t/handler-map handlers)
                            (merge t/default-write-handlers handlers))]
          (t/->Writer
            (TransitFactory/writer (#'t/transit-format type) out handler-map default-handler
                                   (when transform
                                     (reify Function
                                       (apply [_ x]
                                         (transform x)))))))
        (throw (ex-info "Type must be :json, :json-verbose or :msgpack" {:type type}))))))

(defn ^String write-str
  ([x] (write-str x {}))
  ([x
    {:keys [handlers write-meta?]
     :or   {write-meta? true}}]
   #?(:clj
      (let [out    (ByteArrayOutputStream.)
            writer (writer out :json
                           (cond-> {:default-handler unknown-default-handler
                                    :handlers        handlers}
                             write-meta?
                             (assoc :transform t/write-meta)))]
        (t/write writer x)
        (.toString out))

      :cljs
      (let [writer (t/writer :json
                             (cond-> {:handlers (merge cljs-write-handlers handlers)}
                               write-meta?
                               (assoc :transform t/write-meta)))]
        (t/write writer x)))))

#?(:cljs
   (defn envelope-json [msg]
     #js {:transit-message (write-str msg)}))

#?(:cljs
   (defn unpack-json [msg]
     (some-> (gobj/get msg "transit-message") read-str)))
