(ns blog-system.pedestal
  (:require
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.http.route.definition.table :as table]
   [blog-system.pedestal.routes :as routes]
   [io.pedestal.interceptor :refer [interceptor]]))

(defn component-injection
  [datomic mycomponent]
  (interceptor
   {:name ::component-injection
    :enter (fn [x]
             (assoc x :datomic datomic :mycomponent mycomponent))}))

(defn make-routes
  [datomic mycomponent]
  (let [component-injection-interceptor
        (component-injection datomic mycomponent)]
    (->> (routes/make-routes)
         (mapv (fn [r]
                 (update r 2 #(into [component-injection-interceptor] %)))))))

(defn service
  [datomic mycomponent]
  {:env :dev
   ::http/routes (table/table-routes {} (make-routes datomic mycomponent))
   ::http/resource-path "/public"
   ::http/type :jetty
   ::http/join? false
   ::http/port 8080})

(defrecord Pedestal [runnable-service datomic mycomponent]
  component/Lifecycle
  (start [this]
    (if runnable-service
      (do
        (println "server already started")
        this)
      (do
        (println "creating server")
        (let [server-inst (http/create-server (service datomic mycomponent))]
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
