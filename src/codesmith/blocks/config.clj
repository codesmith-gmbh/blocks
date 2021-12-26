(ns codesmith.blocks.config
  (:require [codesmith.blocks :as cb]
            [integrant.core :as ig]
            [aero.core :as aero]
            [clojure.java.io :as io]))

(defmethod cb/block-transform ::configured
  [block-key spec+profile ig-config]
  (assoc ig-config block-key
                   (assoc (spec+profile block-key)
                     :config (ig/ref ::config))))

(defmethod cb/typed-block-transform [::config :inline]
  [block-key spec+profile ig-config]
  (assoc ig-config ::inline (-> spec+profile block-key)))

(defmethod ig/init-key ::inline
  [_ value]
  value)

(derive ::inline ::config)

(defmethod cb/typed-block-transform [::cb/config :aero]
  [block-key spec+profile ig-config]
  (assoc ig-config ::aero (-> spec+profile block-key)))

(defmethod ig/init-key ::aero
  [_ {:keys [file resource environment]}]
  (aero/read-config (let [file (io/file file)]
                      (if (and file (.exists file))
                        file
                        (io/resource (or resource "config.edn"))))
                    {:profile environment}))

(derive ::aero ::config)
