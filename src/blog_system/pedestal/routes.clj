(ns blog-system.pedestal.routes
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "kaebfueiwnfiwef"))

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

