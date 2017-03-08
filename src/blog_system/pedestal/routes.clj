(ns blog-system.pedestal.routes
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [hiccup.core :as hc]
            [blog-system.pedestal.html :as html]
            [io.pedestal.interceptor :refer [interceptor]]
            [blog-system.database.functions :as db.f]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(def home-page
  (interceptor
   {:name ::home-page
    :enter (fn [context]
             (let [uri (-> context :datomic :uri)
                   data (->> (db.f/take-database uri) (into []) sort)]
               (assoc
                 context :response
                 (ring-resp/response
                  (html/make-html 1 "Home" (html/home-content data))))))}))

(def new-post
  (interceptor
   {:name ::new-post
    :enter (fn [context]
             (assoc
               context :response
               (ring-resp/response
                (html/make-html 0 "Create New Post" html/new-post-content))))}))

(def post-ok
  (interceptor
   {:name ::post-ok
    :enter
    (fn [context]
      (let [title (-> context :request :form-params :title)
            content (-> context :request :form-params :content)
            uri (-> context :datomic :uri)]
        (db.f/create-post-database uri title content)
        (assoc
          context :response
          (ring-resp/response
           (html/make-html 0 "Post Successfully Created" html/post-ok-content)))))}))

(def view-post
  (interceptor
   {:name ::view-post
    :enter
    (fn [context]
      (let [postid (-> context :request :path-params :postid)
            id (java.util.UUID/fromString (str postid))
            uri (-> context :datomic :uri)
            data (first (db.f/take-post-by-id uri id))
            tm (data 0)
            title (data 1)
            content (data 2)]
        (assoc
          context :response
          (ring-resp/response
           (html/make-html
            0 (str "Post :: " title) (html/view-post-content id tm title content))))))}))

(def edit-post
  (interceptor
   {:name ::edit-post
    :enter
    (fn [context]
      (let [postid (-> context :request :path-params :postid)
            id (java.util.UUID/fromString (str postid))
            uri (-> context :datomic :uri)
            data (first (db.f/take-post-by-id uri id))
            title (data 1)
            content (data 2)]
        (assoc
          context :response
          (ring-resp/response
           (html/make-html
            0 "Edit Post" (html/edit-post-content id title content))))))}))

(def edit-ok
  (interceptor
   {:name ::edit-ok
    :enter
    (fn [context]
      (let [postid (-> context :request :path-params :postid)
            id (java.util.UUID/fromString (str postid))
            uri (-> context :datomic :uri)
            title (-> context :request :form-params :title)
            content (-> context :request :form-params :content)]
        (db.f/edit-post-database uri id title content)
        (assoc
          context :response
          (ring-resp/response
           (html/make-html 0 "Your Post Successfully Edited" html/edit-ok-content)))))}))


(def delete-confirm
  (interceptor
   {:name ::delete-confirm
    :enter
    (fn [context]
      (let [postid (-> context :request :path-params :postid)]
        (assoc
          context :response
          (ring-resp/response
           (html/make-html 0 "Delete Post Confirmation" (html/delete-confirm-content postid))))))}))

(def delete-ok
  (interceptor
   {:name ::delete-ok
    :enter
    (fn [context]
      (let [postid (-> context :request :path-params :postid)
            uri (-> context :datomic :uri)
            id (java.util.UUID/fromString (str postid))]
        (db.f/delete-post-database uri id)
        (assoc
          context :response
          (ring-resp/response
           (html/make-html 0 "Post Successfully Deleted" html/delete-ok-content)))))}))

(def middlewares
  [(body-params/body-params) http/html-body])

(def routes
  #{["/" :get [home-page] :route-name :home-page]
    ["/about" :get [about-page] :route-name :about]
    ["/new" :get [new-post] :route-name :new-post]
    ["/ok" :post [post-ok] :route-name :post-ok]
    ["/post/:postid" :get [view-post] :route-name :view-post]
    ["/edit/:postid" :get [edit-post] :route-name :edit-post]
    ["/edit-ok/:postid" :post [edit-ok] :route-name :edit-ok]
    ["/delete/:postid" :get [delete-confirm] :route-name :delete-confirm]
    ["/delete-ok/:postid" :post [delete-ok] :route-name :delete-ok]})

(defn make-routes
  []
  (->> routes
       (mapv (fn [r]
               (update r 2 #(into middlewares %))))))

