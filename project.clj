(defproject trptcolin/trymplicit "0.0.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;; needs release of
                 ;; https://github.com/ztellman/riddley/pull/13 in order to
                 ;; bump to non-SNAPSHOT
                 [riddley "0.1.8-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[speclj "3.0.2"]]}}
  :plugins [[speclj "3.0.2"]]
  :test-paths ["spec"])
