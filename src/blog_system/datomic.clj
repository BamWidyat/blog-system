(ns blog-system.datomic
  (:require
   [com.stuartsierra.component :as component]
   [datomic.api :as d]))

(defrecord Datomic [uri]
  component/Lifecycle
  (start [this]
    (println "test datomic print start")
    this)
  (stop [this]
    (println "test datomic print stop")
    this))

(defn make
  [{:keys [uri] :as datomic-config}]
  (map->Datomic {:uri uri}))
