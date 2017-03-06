(ns blog-system.pedestal.routes
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [hiccup.core :as hc]
            [blog-system.pedestal.html :as html]
            [io.pedestal.interceptor :refer [interceptor]]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(def home-page
  (interceptor
   {:name ::home-page
    :enter (fn [context]
             (assoc
              context :response
              {:status 200 :body (str (-> context :datomic :uri))}))}))

#_(defn home-page
  [request]
  (ring-resp/response
   #_(html/make-html 1 "Home" (html/home-content []))))

(def middlewares
  [(body-params/body-params) http/html-body])

(def routes
  #{["/" :get [home-page] :route-name :home]
    ["/about" :get [about-page] :route-name :about]})

(defn make-routes
  []
  (->> routes
       (mapv (fn [r]
               (update r 2 #(into middlewares %))))))

