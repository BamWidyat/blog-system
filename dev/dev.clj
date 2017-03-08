(ns dev
  (:require
   [com.stuartsierra.component :as component]
   [clojure.tools.namespace.repl :as ns-repl]
   [datomic.api :as d]
   [blog-system.system :as system]))

(defonce dev-system nil)

(def uri "datomic:mem://blog")

(defn dev-config
  "return development configuration map"
  []
  {:datomic {:uri uri}})

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


(defn take-database-id []
  (d/q '[:find ?id
              :where
              [?e :post/id ?id]]
            (d/db (d/connect uri))))

(defn take-database []
  (d/q '[:find ?id ?title ?content ?time
              :where
              [?e :post/title ?title]
              [?e :post/id ?id]
              [?e :post/content ?content]
              [?e :post/time ?time]]
            (d/db (d/connect uri))))

(defn take-post-by-id [id]
  (d/q '[:find ?id ?title ?content ?time
              :in $ ?id
              :where
              [?e :post/id ?id]
              [?e :post/title ?title]
              [?e :post/content ?content]
              [?e :post/time ?time]]
            (d/db (d/connect uri)) id))

(defn delete-post-database [id]
  (d/transact
   (d/connect uri)
   [[:db.fn/retractEntity [:post/id id]]]))

(defn go-db []
  (d/create-database uri)
  (d/transact (d/connect uri) schema))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (ns-repl/refresh :after 'dev/go))
