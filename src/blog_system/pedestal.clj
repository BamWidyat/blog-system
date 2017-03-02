(ns blog-system.pedestal
  (:require
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.http.route.definition.table :as table]
   [blog-system.pedestal.routes :as routes]))

(def service
  {:env :dev
   ::http/routes (table/table-routes {} (routes/make-routes))
   ::http/resource-path "/public"
   ::http/type :jetty
   ::http/join? false
   ::http/port 8080})

(defrecord Pedestal [runnable-service]
  component/Lifecycle
  (start [this]
    (if runnable-service
      (do
        (println "server already started")
        this)
      (do
        (println "creating server")
        (let [server-inst (http/create-server service)]
          (http/start server-inst)
          (assoc this :runnable-service server-inst)))))
  (stop [this]
    (if runnable-service
      (do
        (println "stopping the server")
        (http/stop runnable-service)
        (dissoc this :runnable-service))
      this))
  )

(defn make
  []
  (map->Pedestal {}))
