# Transito

Transito is a library with helpers to easy the usage of Transit in Clojure and Clojurescript.

Why use Transito?

Transito provides an easy way to an opinionated way to use Transit:

1. Simple `write-str` and `read-str` functions allows CLJC usage of Transit without dealing with `ByteArray` classes or similar things.
2. Support out-of-the-box write unknown types tolerance.
3. Metadata support enabled by default.

## Install

```
com.wsscode/transito {:mvn/version "2021.07.04"}
```

## API

```clojure
(ns transito-demo
  (:require [com.wsscode.transito :as transito]))

; write to string
(def demo-str (transito/write-str {:data "structure"}))

; read from string
(def data (transito/read-str demo-str))

; options for write-str
(def demo-str (transito/write-str {:data "structure"}
                {:handlers {...}
                 :default-handler (some-handler) ; use this to override the default handler for unknown types 
                 :write-meta? false ; convenience flag to enable or disable write-meta, enabled by default
                 }))

; options for read-str
(def data (transito/read-str demo-str
            {:handlers {...}}))
```
