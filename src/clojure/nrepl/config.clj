(ns nrepl.config
  "Server configuration utilities.
  Some server defaults can be configured via
  a configuration file or env variables.
  This namespace provides convenient API to work
  with them."
  {:author "Bozhidar Batsov"}
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]))

(def ^:private home-dir
  "The user's home directory."
  (System/getProperty "user.home"))

(def config-dir
  "nREPL's configuration directory.
  By default it's ~/.nrepl, but this can be overridden
  with the NREPL_CONFIG_DIR env variable."
  (or (System/getenv "NREPL_CONFIG_DIR")
      (str home-dir java.io.File/separator ".nrepl")))

(def config-file
  "nREPL's config file."
  (str config-dir java.io.File/separator "config.edn"))

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (try
    (with-open [r (io/reader source)]
      (edn/read (java.io.PushbackReader. r)))

    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))

(defn load-config []
  (let [config-file (io/file config-file)]
    (if (.exists config-file)
      (load-edn config-file)
      {})))

(def config
  "Configuration map."
  (load-config))

(defn bind-address []
  (or (System/getenv "NREPL_BIND_ADDRESS") (:bind config)))

(defn port []
  (or (System/getenv "NREPL_PORT") (:port config)))

(defn transport []
  (or (System/getenv "NREPL_TRANSPORT") (:transport config)))
