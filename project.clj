(defproject blog-system "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.pedestal/pedestal.service "0.5.2"]

                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.5.2"]
                 ;; [io.pedestal/pedestal.immutant "0.5.2"]
                 ;; [io.pedestal/pedestal.tomcat "0.5.2"]

                 [ch.qos.logback/logback-classic "1.1.8" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.22"]
                 [org.slf4j/jcl-over-slf4j "1.7.22"]
                 [org.slf4j/log4j-over-slf4j "1.7.22"]
                 [org.slf4j/slf4j-simple "1.7.21"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.datomic/datomic-free "0.9.5561"]
                 [hiccup "1.0.5"]
                 [buddy/buddy-hashers "1.2.0"]
                 [io.rkn/conformity "0.4.0"]
                 [garden "1.3.2"]]
  :min-lein-version "2.0.0"
  :jvm-opts ["-Xms256m" "-Xms256m"]
  :resource-paths ["config", "resources"]
  ;; If you use HTTP/2 or ALPN, use the java-agent to pull in the correct alpn-boot dependency
  ;:java-agents [[org.mortbay.jetty.alpn/jetty-alpn-agent "2.0.5"]]
  :profiles {:dev {:dependencies [[io.pedestal/pedestal.service-tools "0.5.2"]]
                   :source-paths ["dev" "config"]}
             :uberjar {:aot :all}}
  :main ^{:skip-aot true} blog-system.core
  :repl-options {:init-ns user})

;;change

