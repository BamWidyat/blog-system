(ns blog-system.system
  (:require
   [com.stuartsierra.component :as component]
   [blog-system.pedestal :as pedestal]
   [blog-system.datomic :as datomic]))

(defn system
  [config]
  (component/system-map
   :config config
   :mycomponent {:my :component}
   :datomic (datomic/make (-> config :datomic))
   :pedestal (pedestal/make)))
