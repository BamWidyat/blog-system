(ns dev
  (:require
   [com.stuartsierra.component :as component]
   [clojure.tools.namespace.repl :as ns-repl]
   [datomic.api :as d]
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

(def schema
  [{:db/ident :post/id
    :db/valueType :db.type/uuid
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :post/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :post/content
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :post/time
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one}])

(defn prepare-database []
  (d/create-database "datomic:mem://blog")
  (d/transact (d/connect "datomic:mem://blog")))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (ns-repl/refresh :after 'dev/go))
