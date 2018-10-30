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
  (with-open [r (io/reader source)]
    (edn/read (java.io.PushbackReader. r))))

(defn load-config [file]
  (let [config-file (io/file config-file)]
    (if (.exists config-file)
      (load-edn config-file)
      {})))

(def config
  "Configuration map.
  It's created by merging the global configuration file
  with a local configuration file that would normally
  the placed in the directory in which you're running
  nREPL."
  (merge
   (load-config config-file)
   (load-config ".nrepl-config.edn")))

(defn bind-address []
  (or
   (System/getenv "NREPL_BIND_ADDRESS")
   (:bind config)))

(defn port
  "The default port for the server to listen on.
  First we check the env variable NREPL_PORT,
  then `config`."
  []
  (if-let [env-port (System/getenv "NREPL_PORT")]
    (Integer/parseInt env-port)
    (:port config)))

(defn transport []
  (or
   (System/getenv "NREPL_TRANSPORT")
   (:transport config)))
