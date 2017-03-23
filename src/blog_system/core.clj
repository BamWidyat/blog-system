(ns blog-system.core
  (:gen-class)
  (:require
   [com.stuartsierra.component :as component]
   [blog-system.system :as system]
   [clojure.edn :as edn]
   [datomic.api :as d]))

(defn config
  []
  (edn/read-string (slurp "config.edn")))

(defn uri [] (-> (config) :datomic :uri))

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
    :db/cardinality :db.cardinality/one}
   {:db/ident :post/username
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :user/name
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :user/password
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}])

(defn start-db []
  (println "Starting database . . .")
  (d/create-database (uri))
  (d/transact (d/connect (uri)) schema))

(defn -main
  [& args]
  (let [main-config config]
    (start-db)
    (component/start (system/system (config)))))
