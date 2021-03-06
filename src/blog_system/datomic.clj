(ns blog-system.datomic
  (:require
   [com.stuartsierra.component :as component]
   [datomic.api :as d]))

(defrecord Datomic [uri]
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    this))

(defn make
  [{:keys [uri] :as datomic-config}]
  (map->Datomic {:uri uri}))
