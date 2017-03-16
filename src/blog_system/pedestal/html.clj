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

(defn home-content [data]
   [[:div {:class "jumbotron text-center"}
    [:h1 "Home Page"]
    [:p "Welcome to blog test page"]]
   [:div {:class "container"}
    (if (empty? data)
      [:div {:class "container-fluid"}
       [:div {:class "alert alert-info"}
        [:div [:strong "You have no post yet!"] " Click the new post button to create a new post"]]]
      [:div {:class "row"}
       (for [post-data data]
         [:div {:class "col-sm-4"}
          [:h5 [:small (str "Post ID: " (str (post-data 1)))]]
          [:a {:href (str "/post/" (post-data 1))} [:h3 (post-data 2)]]
          [:h5 [:small (str "Posted at " (post-data 0))]]
          (post-data 3)[:br][:br][:br]])])
    [:div {:class "text-center"}
     [:br][:br]
     [:a {:href "/new"}
      [:button {:class "btn btn-primary" :type "button"} "New Post"]]
     [:br][:br]]]])

(def modal-test
  [[:div {:class "container"}
    [:h2 "Modal Example"]
    [:button {:type "button" :class "btn btn-info btn-lg" :data-toggle "modal" :data-target "#myModal"} "Open Modal"]
    [:div {:class "modal fade" :id "myModal" :role "dialog"}
     [:div {:class "modal-dialog"}
      [:div {:class "modal-content"}
       [:div {:class "modal-header"}
        [:button {:type "button" :class "close" :data-dismiss "modal"}"close"]
        [:h4 {:class "modal-title"} "Modal Header"]]
       [:div {:class "modal-body"}
        [:p "Some text in the modal body"]]
       [:div {:class "modal-footer"}
        [:button {:type "button" :class "btn btn-default" :data-dismiss "modal"} "Close"]]]]]]])

(def new-post-content
  [[:div {:align "center"}
    [:h1 "Create New Post"]]
   [:div {:class "container"}
    [:form {:action "/result" :method "post" :id "input-form"}
     [:div {:class "form-group"}
      [:label {:for "title"} "Title"]
      [:input {:type "text" :class "form-control" :id "title" :name "title" :required ""}]]
     [:div {:class "form-group"}
      [:label {:for "content"} "Content"]
      [:textarea {:class "form-control" :rows "20" :id "content" :name "content" :required ""}]]
     [:div {:class "text-center"}
      [:div {:class "btn-group"}
       [:a {:href "/" :class "btn btn-primary"} "Cancel"]
       [:button {:type "reset" :class "btn btn-primary"} "Reset"]
       [:button {:type "submit" :class "btn btn-primary"} "Submit"]]]]]])

(def post-ok-content
  [[:div {:class "container-fluid"}
    [:div {:class "alert alert-success"}
     [:strong "Congratulations!"] " Your post successfully created!"]
    [:div {:class "text-center"}
     [:div {:class "btn-group"}
      [:a {:href "/new" :class "btn btn-primary"} "New Post"]
      [:a {:href "/" :class "btn btn-primary"} "Home"]]]]])

(def post-failed-content
  [[:div {:class "container-fluid"}
    [:div {:class "alert alert-danger"}
     [:strong "Failed!"] " An error has occured that makes your post failed to be created.
     Please try again in a few minutes or contact administrator if the problem presist."]
    [:div {:class "text-center"}
     [:div {:class "btn-group"}
      [:a {:href "/new" :class "btn btn-primary"} "New Post"]
      [:a {:href "/" :class "btn btn-primary"} "Home"]]]]])

(defn view-post-content [id tm title content]
  [[:div {:class "container"}
    [:h5 [:small (str "Post ID: " id)]]
    [:h1 [:strong (str title)]]
    [:h5 [:small (str "Posted at " tm)]]
    [:br]
    (str content)
    [:br][:br]
    [:div {:class "text-center"}
     [:div {:class "btn-group"}
      [:a {:href "/" :class "btn btn-primary"} "Home"]
      [:a {:href (str "/delete/" id) :class "btn btn-primary"} "Delete"]
      [:a {:href (str "/edit/" id) :class "btn btn-primary"} "Edit"]]]]])

(defn edit-post-content [id title content]
  [[:div {:align "center"}
    [:h1 "Edit Post"]]
   [:div {:class "container"}
    [:form {:action (str "/edit-ok/" id) :method "post" :id "input-form"}
     [:div {:class "form-group"}
      [:label {:for "title"} "Title"]
      [:input {:type "text" :class "form-control" :id "title" :name "title" :value (str title)}]]
     [:div {:class "form-group"}
      [:label {:for "content"} "Content"]
      [:textarea {:class "form-control" :rows "20" :id "content" :name "content"} (str content)]]
     [:div {:class "text-center"}
      [:div {:class "btn-group"}
       [:a {:href (str "/post/" id) :class "btn btn-primary"} "Cancel"]
       [:button {:type "reset" :class "btn btn-primary"} "Reset"]
       [:button {:type "submit" :class "btn btn-primary"} "Edit"]]]]]])

(def edit-ok-content
  [[:div {:class "container-fluid"}
    [:div {:class "alert alert-success"}
     [:strong "Congratulations!"] " Your post successfully edited!"]
    [:div {:class "text-center"}
     [:a {:href "/" :class "btn btn-primary"} "Home"]]]])

(defn delete-confirm-content [postid]
  [[:div {:class "container-fluid"}
    [:div {:class "alert alert-warning"} [:strong (str "You are about to delete this post!")]
     " Deleted post cannot be recovered, are you sure you delete this post?"]
    [:br][:br]
    [:div {:class "text-center"}
     [:form {:action (str "/delete-ok/" postid) :method "post"}
      [:a {:href (str "/post/" postid) :class "btn btn-default"} "Cancel"]
      "     "
      [:button {:type "submit" :class "btn btn-primary"} "Yes"]]]]])

(def delete-ok-content
  [[:div {:class "container-fluid"}
    [:div {:class "alert alert-success"} [:strong "Congratulations!"] " Your post successfully deleted!"]
    [:br][:br]
    [:div {:class "text-center"}
     [:a {:href "/" :class "btn btn-primary"} "Go to Home"]]]])
