(ns dev
  (:require
   [com.stuartsierra.component :as component]
   [clojure.tools.namespace.repl :as ns-repl]

   [blog-system.system :as system]))

(defonce dev-system nil)

(defn dev-config
  "return development configuration map"
  []
  {:datomic {:uri "datomic:mem://blog"}})

(defn start []
  (alter-var-root #'dev-system component/start))

(defn init []
  (alter-var-root #'dev-system
                  (constantly (system/system (dev-config)))))

(defn stop []
  (alter-var-root #'dev-system
                  (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (ns-repl/refresh :after 'dev/go))
