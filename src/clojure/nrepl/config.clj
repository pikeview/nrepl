(ns nrepl.config
  "Server configuration utilities.
  Some server defaults can be configured via configuration
  files (local or global) or env variables.  This namespace provides
  convenient API to work with them.

  The config resolution algorithm is the following:

  * The global config file .nrepl/config.edn is merged with
  any local config file (.nrepl-config.edn) if present.
  The values in the local config file take precedence.
  * If some configuration option is specified by an
  environment variable, the env variable will take
  precedence over whatever is in the config files."
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

(defn- load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (with-open [r (io/reader source)]
    (edn/read (java.io.PushbackReader. r))))

(defn- load-config
  "Load the configuration `file`.
  Return its contents as EDN if the file exists,
  or an empty map otherwise."
  [file]
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

(defn bind-address
  "The default bind address for the server."
  []
  (or
   (System/getenv "NREPL_BIND_ADDRESS")
   (:bind config)))

(defn port
  "The default port for the server to listen on."
  []
  (if-let [env-port (System/getenv "NREPL_PORT")]
    (Integer/parseInt env-port)
    (:port config)))

(defn transport
  "The default transport for the server."
  []
  (or
   (System/getenv "NREPL_TRANSPORT")
   (:transport config)))
