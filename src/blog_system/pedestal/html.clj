(ns blog-system.pedestal.html
  (:require [hiccup.core :as hc]))

(defn bootstrap []
  (for [cnt (range 4)]
    ([[:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"}]
      [:script {:src "https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"}]
      [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"}]] cnt)))

(defn navpanel [active]
  [:nav {:class "navbar navbar-inverse"}
   [:div {:class "container-fluid"}
    [:div {:class "navbar-header"}
     [:a {:class "navbar-brand" :href "/"} "BlogWeb"]]
    [:ul {:class "nav navbar-nav"}
     (if (= active 1)
       [:li {:class "active"} [:a {:href "/"} "Home"]]
       [:li [:a {:href "/"} "Home"]])
     [:li [:a {:href "/"} "About"]]]
    [:ul {:class "nav navbar-nav navbar-right"}
     [:li [:a {:href "#"} [:span {:class "glyphicon glyphicon-user"}] " Sign Up"]]
     [:li [:a {:href "#"} [:span {:class "glyphicon glyphicon-log-in"}] " Login"]]]]])

(defn make-html [nav-val title [& content]]
  (hc/html
   [:html
    [:head
     [:title (str title)]
     (bootstrap)]
    [:body
     (navpanel nav-val)
     content]]))

(defn home-content [id]
   [[:div {:class "jumbotron text-center"}
    [:h1 "Home Page"]
    [:p "Welcome to blog test page"]]
   [:div {:class "container"}
    (if (empty? id)
      [:div {:class "container-fluid"}
       [:div {:class "alert alert-info"}
        [:div [:strong "You have no post yet!"] " Click the new post button to create a new post"]]]
      [:div {:class "row"}
       "A"])
    [:div {:class "text-center"}
     [:br][:br]
     [:a {:href "/new"}
      [:button {:class "btn btn-primary" :type "button"} "New Post"]]
     [:br][:br]]]])