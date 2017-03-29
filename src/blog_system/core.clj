(ns blog-system.core
  (:gen-class)
  (:require
   [com.stuartsierra.component :as component]
   [blog-system.system :as system]
   [clojure.edn :as edn]
   [datomic.api :as d]
   [io.rkn.conformity :as c]))

(defn config
  []
  (edn/read-string (slurp "config.edn")))

(defn uri [] (-> (config) :datomic :uri))

(def schema {:blog-schema
 {:txes
  [[{:db/ident :post/id
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
     :db/cardinality :db.cardinality/one}
    {:db/ident :comment/id
     :db/valueType :db.type/uuid
     :db/unique :db.unique/identity
     :db/cardinality :db.cardinality/one}
    {:db/ident :comment/user
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one}
    {:db/ident :comment/comment
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one}
    {:db/ident :comment/reply
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/many}]]}})

;; (def schema
;;   [{:db/ident :post/id
;;     :db/valueType :db.type/uuid
;;     :db/unique :db.unique/identity
;;     :db/cardinality :db.cardinality/one}
;;    {:db/ident :post/title
;;     :db/valueType :db.type/string
;;     :db/cardinality :db.cardinality/one}
;;    {:db/ident :post/content
;;     :db/valueType :db.type/string
;;     :db/cardinality :db.cardinality/one}
;;    {:db/ident :post/time
;;     :db/valueType :db.type/instant
;;     :db/cardinality :db.cardinality/one}
;;    {:db/ident :post/username
;;     :db/valueType :db.type/ref
;;     :db/cardinality :db.cardinality/one}
;;    {:db/ident :user/name
;;     :db/valueType :db.type/string
;;     :db/unique :db.unique/identity
;;     :db/cardinality :db.cardinality/one}
;;    {:db/ident :user/password
;;     :db/valueType :db.type/string
;;     :db/cardinality :db.cardinality/one}])

(defn start-db []
  (println "Starting database . . .")
  (d/create-database (uri))
  #_(d/transact (d/connect (uri)) schema)
  (c/ensure-conforms (d/connect (uri)) schema [:blog-schema]))

(defn -main
  [& args]
  (let [main-config config]
    (start-db)
    (component/start (system/system (config)))))

