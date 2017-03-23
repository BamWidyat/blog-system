(ns blog-system.pedestal.routes
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [hiccup.core :as hc]
            [blog-system.pedestal.html :as html]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.interceptor.chain :refer [terminate]]
            [io.pedestal.http.ring-middlewares :as p.middlewares]
            [blog-system.database.functions :as db.f]
            [datomic.api :as d]))

(def home-page
  (interceptor
   {:name ::home-page
    :enter (fn [context]
             (let [session (-> context :request :session)]
               (assoc
                 context :response
                 (ring-resp/response
                  (html/make-html session "Home" (html/home-content session))))))}))

(def post-list
  (interceptor
   {:name ::post-list
    :enter (fn [context]
             (let [uri (-> context :datomic :uri)
                   data (->> (db.f/take-database uri) sort)
                   session (-> context :request :session)]
               (assoc
                 context :response
                 (ring-resp/response
                  (html/make-html session "Post List" (html/post-list-content data session))))))}))

(def new-post
  (interceptor
   {:name ::new-post
    :enter (fn [context]
             (let [session (-> context :request :session)]
               (assoc
                 context :response
                 (ring-resp/response
                  (html/make-html session "Create New Post" html/new-post-content)))))}))

(def user-signup
  (interceptor
   {:name ::user-signup
    :enter (fn [context]
             (let [session (-> context :request :session)]
               (assoc
                 context :response
                 (ring-resp/response
                  (html/make-html session "New User Sign Up" (html/user-signup-content "No"))))))}))

(def user-signup-check
  (interceptor
   {:name ::user-signup-check
    :enter (fn [context]
             (let [uri (-> context :datomic :uri)
                   session (-> context :request :session)
                   username (-> context :request :form-params :username)
                   pass (-> context :request :form-params :password)
                   re-pass (-> context :request :form-params :re-password)
                   response (cond
                             (< (count username) 4) (html/make-html session "New User Signup" (html/user-signup-content "username-short"))
                             (> (count username) 16) (html/make-html session "New User Signup" (html/user-signup-content "username-long"))
                             (not (empty? (db.f/check-username-database uri username))) (html/make-html session "New User Signup" (html/user-signup-content "username-exist"))
                             (< (count pass) 6) (html/make-html session "New User Signup" (html/user-signup-content "password-short"))
                             (not= pass re-pass) (html/make-html session "New User Signup" (html/user-signup-content "password-miss"))
                             :else "OK")]
               (if (= response "OK")
                 (do
                   (db.f/user-signup uri username pass)
                   (if (empty? (db.f/check-username-database uri username))
                     (assoc context :response (ring-resp/response (html/make-html session "Registration Failed!" html/signup-failed-content)))
                     (assoc context :response (ring-resp/response (html/make-html session "Registration Success!" html/signup-ok-content)))))
                 (assoc context :response (ring-resp/response response)))))}))

(def user-login
  (interceptor
   {:name ::user-login
    :enter (fn [context]
             (let [session (-> context :request :session)]
               (assoc
                 context :response
                 (ring-resp/response
                  (html/make-html session "User Login" (html/user-login-content "No"))))))}))

(def user-login-check
  (interceptor
   {:name ::user-login-check
    :enter (fn [context]
             (let [uri (-> context :datomic :uri)
                   session (-> context :request :session)
                   username (-> context :request :form-params :username)
                   pass (-> context :request :form-params :password)]
               (if (empty? (db.f/check-username-database uri username))
                 (assoc context :response (ring-resp/response (html/make-html session "User Login" (html/user-login-content "error"))))
                 (if (db.f/check-user-password uri username pass)
                   (assoc context :response (-> (ring-resp/redirect "/")
                                                (assoc-in [:session :user] username)))
                   (assoc context :response (ring-resp/response (html/make-html session "User Login" (html/user-login-content "error"))))))))}))

(def post-result
  (interceptor
   {:name ::post-result
    :enter
    (fn [context]
      (let [title (-> context :request :form-params :title)
            content (-> context :request :form-params :content)
            uri (-> context :datomic :uri)
            id (d/squuid)
            session (-> context :request :session)
            username (session :user)]
        (db.f/create-post-database uri id title content username)
        (if (empty? (db.f/take-post-by-id uri id))
          (assoc
          context :response
          (ring-resp/response
           (html/make-html session "Failed to Create Post" html/post-failed-content)))
          (assoc
          context :response
          (ring-resp/response
           (html/make-html session "Post Successfully Created" html/post-ok-content))))))}))

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
            content (data 2)
            username (data 3)
            session (-> context :request :session)]
        (assoc
          context :response
          (ring-resp/response
           (html/make-html
            session (str "Post :: " title) (html/view-post-content id tm title content username session))))))}))

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
            content (data 2)
            session (-> context :request :session)]
        (assoc
          context :response
          (ring-resp/response
           (html/make-html
            session "Edit Post" (html/edit-post-content id title content))))))}))

(def edit-ok
  (interceptor
   {:name ::edit-ok
    :enter
    (fn [context]
      (let [postid (-> context :request :path-params :postid)
            id (java.util.UUID/fromString (str postid))
            uri (-> context :datomic :uri)
            title (-> context :request :form-params :title)
            content (-> context :request :form-params :content)
            session (-> context :request :session)]
        (db.f/edit-post-database uri id title content)
        (assoc
          context :response
          (ring-resp/response
           (html/make-html session "Your Post Successfully Edited" html/edit-ok-content)))))}))


(def delete-confirm
  (interceptor
   {:name ::delete-confirm
    :enter
    (fn [context]
      (let [postid (-> context :request :path-params :postid)
            session (-> context :request :session)]
        (assoc
          context :response
          (ring-resp/response
           (html/make-html session "Delete Post Confirmation" (html/delete-confirm-content postid))))))}))

(def delete-ok
  (interceptor
   {:name ::delete-ok
    :enter
    (fn [context]
      (let [postid (-> context :request :path-params :postid)
            uri (-> context :datomic :uri)
            id (java.util.UUID/fromString (str postid))
            session (-> context :request :session)]
        (db.f/delete-post-database uri id)
        (assoc
          context :response
          (ring-resp/response
           (html/make-html session "Post Successfully Deleted" html/delete-ok-content)))))}))

(def user-logout
  (interceptor
   {:name ::user-logout
    :enter (fn [context]
             (let [session (-> context :request :session)]
               (assoc
                 context :response
                 (-> (ring-resp/redirect "/") (assoc :session {})))))}))

(def session-check
  (interceptor
   {:name ::session-check
    :enter (fn [context]
             (let [session (-> context :request :session)
                   page-uri (-> context :request :uri)
                   postid (-> context :request :path-params :postid)
                   free-page (or
                              (= page-uri "/")
                              (= page-uri "/post")
                              (= page-uri "/login")
                              (= page-uri "/signup")
                              (= page-uri (str "/post/" postid)))
                   need-logout (or
                                (= page-uri "/signup")
                                (= page-uri "/login"))]
               (if (empty? session)
                 (if free-page
                   context
                   (do
                     (terminate context)
                     (assoc context :response (ring-resp/response "login 1st"))))
                 (if need-logout
                   (do
                     (terminate context)
                     (assoc context :response (ring-resp/response "logout 1st")))
                   context))))}))

(def middlewares
  [session-check #_(p.middlewares/session) (body-params/body-params) http/html-body])

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

(def test-page
  (interceptor
   {:name ::test-page
    :enter (fn [context]
             (assoc context :response (assoc (ring-resp/response (str context)) :session {:user "TEST"})))}))

(def routes
  #{["/" :get [home-page] :route-name :home-page]
    ["/post" :get [post-list] :route-name :post-list]
    ["/new" :get [new-post] :route-name :new-post]
    ["/signup" :get [user-signup] :route-name :user-signup]
    ["/signup" :post [user-signup-check] :route-name :user-signup-check]
    ["/login" :get [user-login] :route-name :user-login]
    ["/login" :post [user-login-check] :route-name :user-login-check]
    ["/logout" :get [user-logout] :route-name :user-logout]
    ["/result" :post [post-result] :route-name :post-result]
    ["/post/:postid" :get [view-post] :route-name :view-post]
    ["/edit/:postid" :get [edit-post] :route-name :edit-post]
    ["/edit-ok/:postid" :post [edit-ok] :route-name :edit-ok]
    ["/delete/:postid" :get [delete-confirm] :route-name :delete-confirm]
    ["/delete-ok/:postid" :post [delete-ok] :route-name :delete-ok]
    ["/show-session" :get [show-session] :route-name :show-session]
    ["/out-session" :get [out-session] :route-name :log-out]
    ["/test-page" :get [test-page] :route-name :test-page]
    })

(defn make-routes
  []
  (->> routes
       (mapv (fn [r]
               (update r 2 #(into middlewares %))))))

