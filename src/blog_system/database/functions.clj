(ns blog-system.database.functions
  (:require [datomic.api :as d]))

(defn create-post-database [uri title content]
  (d/transact
   (d/connect uri)
   [{:post/id (d/squuid)
     :post/title (str title)
     :post/content (str content)
     :post/time (new java.util.Date)}]))

(defn take-database [uri]
  (d/q '[:find ?time ?id ?title ?content
              :where
              [?e :post/title ?title]
              [?e :post/id ?id]
              [?e :post/content ?content]
              [?e :post/time ?time]]
            (d/db (d/connect uri))))

(defn take-post-by-id [uri id]
  (d/q '[:find ?time ?title ?content
              :in $ ?id
              :where
              [?e :post/id ?id]
              [?e :post/title ?title]
              [?e :post/content ?content]
              [?e :post/time ?time]]
            (d/db (d/connect uri)) id))

