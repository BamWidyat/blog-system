(ns blog-system.database.functions
  (:require
   [datomic.api :as d]
   [buddy.hashers :as h]))

(defn create-post-database [uri id title content username]
  (d/transact
   (d/connect uri)
   [{:post/id id
     :post/title (str title)
     :post/content (str content)
     :post/username [:user/name username]
     :post/time (new java.util.Date)}]))

(defn user-signup [uri username password]
  (d/transact
   (d/connect uri)
   [{:user/name username
     :user/password (h/derive password)}]))

(defn check-username-database [uri username]
  (d/q '[:find ?username
              :in $ ?username
              :where
              [?e :user/name ?username]]
            (d/db (d/connect uri)) username))

(defn check-user-password [uri username password]
  (h/check
   password (-> (d/q '[:find ?password
                       :in $ ?username
                       :where
                       [?e :user/name ?username]
                       [?e :user/password ?password]]
                     (d/db (d/connect uri)) username) first first)))

(defn take-database [uri]
  (d/q '[:find ?time ?id ?title ?content ?username
              :where
              [?e :post/title ?title]
              [?e :post/id ?id]
              [?e :post/content ?content]
              [?e :post/time ?time]
              [?e :post/username ?u]
              [?u :user/name ?username]]
            (d/db (d/connect uri))))

(defn take-post-by-id [uri id]
  (d/q '[:find ?time ?title ?content ?username
              :in $ ?id
              :where
              [?e :post/id ?id]
              [?e :post/title ?title]
              [?e :post/content ?content]
              [?e :post/time ?time]
              [?e :post/username ?u]
              [?u :user/name ?username]]
            (d/db (d/connect uri)) id))

(defn edit-post-database [uri id title content]
  (d/transact
   (d/connect uri)
   [[:db/add [:post/id id]
     :post/title title]
    [:db/add [:post/id id]
     :post/content content]]))

(defn delete-post-database [uri id]
  (d/transact
   (d/connect uri)
   [[:db.fn/retractEntity [:post/id id]]]))

