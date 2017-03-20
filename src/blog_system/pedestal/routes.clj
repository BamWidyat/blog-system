(ns blog-system.pedestal.routes
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [hiccup.core :as hc]
            [blog-system.pedestal.html :as html]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.http.ring-middlewares :as p.middlewares]
            [blog-system.database.functions :as db.f]
            [datomic.api :as d]))

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

(def user-signup
  (interceptor
   {:name ::user-signup
    :enter (fn [context]
             (assoc
               context :response
               (ring-resp/response
                (html/make-html 0 "New User Signup" (html/user-signup-content "No")))))}))

(def user-signup-result
  (interceptor
   {:name ::user-signup-result
    :enter (fn [context]
             (let [username (-> context :request :form-params :username)
                   pass (-> context :request :form-params :password)
                   re-pass (-> context :request :form-params :re-password)]
               (assoc
                 context :response
                 (ring-resp/response
                   (cond
                     (< (count username) 4) (html/make-html 0 "New User Signup" (html/user-signup-content "username-short"))
                     (> (count username) 16) (html/make-html 0 "New User Signup" (html/user-signup-content "username-long"))
                     (< (count pass) 6) (html/make-html 0 "New User Signup" (html/user-signup-content "password-short"))
                     (not= pass re-pass) (html/make-html 0 "New User Signup" (html/user-signup-content "password-miss"))
                     :else "OK")))))}))

(def post-result
  (interceptor
   {:name ::post-result
    :enter
    (fn [context]
      (let [title (-> context :request :form-params :title)
            content (-> context :request :form-params :content)
            uri (-> context :datomic :uri)
            id (d/squuid)]
        (db.f/create-post-database uri id title content)
        (if (empty? (db.f/take-post-by-id uri id))
          (assoc
          context :response
          (ring-resp/response
           (html/make-html 0 "Failed to Create Post" html/post-failed-content)))
          (assoc
          context :response
          (ring-resp/response
           (html/make-html 0 "Post Successfully Created" html/post-ok-content))))))}))

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
  [(p.middlewares/session) (body-params/body-params) http/html-body])

(def show-session
  (interceptor
    {:name ::show-session
     :enter (fn [context]
              (-> context
                  (assoc :response
                    (-> (ring-resp/response (-> context
                                                :request
                                                :session
                                                str))
                        (assoc-in [:session :counter] (let [c (-> context
                                                                  :request
                                                                  :session
                                                                  :counter)]
                                                        (if c (inc c) 1)
                                                        ))))))}))

(def out-session
  (interceptor
    {:name ::out-session
     :enter (fn [context]
              (-> context
                  (assoc :response
                    (-> (ring-resp/redirect "/show-session")
                        (assoc-in [:session :counter] nil)))))}))

(def routes
  #{["/" :get [home-page] :route-name :home-page]
    ["/about" :get [about-page] :route-name :about]
    ["/new" :get [new-post] :route-name :new-post]
    ["/signup" :get [user-signup] :route-name :user-signup]
    ["/signup" :post [user-signup-result] :route-name :user-signup-result]
    ["/result" :post [post-result] :route-name :post-result]
    ["/post/:postid" :get [view-post] :route-name :view-post]
    ["/edit/:postid" :get [edit-post] :route-name :edit-post]
    ["/edit-ok/:postid" :post [edit-ok] :route-name :edit-ok]
    ["/delete/:postid" :get [delete-confirm] :route-name :delete-confirm]
    ["/delete-ok/:postid" :post [delete-ok] :route-name :delete-ok]
    ["/show-session" :get [show-session] :route-name :show-session]
    ["/out-session" :get [out-session] :route-name :log-out]
    })

(defn make-routes
  []
  (->> routes
       (mapv (fn [r]
               (update r 2 #(into middlewares %))))))

