(ns user)

(defn dev
  []
  (require 'dev)
  (in-ns 'dev))

(defn devsystem
  []
  (require 'devsystem)
  (in-ns 'devsystem))

(defn experiment
  []
  (require 'experiment)
  (in-ns 'experiment))

(defn go
  []
  (println "Don't you mean (dev) then (go) ?"))
