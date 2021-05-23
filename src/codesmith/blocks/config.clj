(ns codesmith.blocks.config
  (:require [codesmith.blocks :as cb]
            [integrant.core :as ig]
            [clojure.edn :as edn]))

(defn- substitute-inline [ig-config system+profile block-key inline-block-key]
  (cb/substitute-on-vec ig-config
                     block-key inline-block-key
                     (fn [_ config]
                       ((-> system+profile block-key) (select-keys config [:block-name :parameter-name])))))

(defmethod cb/typed-block-transform [::cb/config :inline]
  [block-key system+profile ig-config]
  (substitute-inline ig-config system+profile
                     block-key ::inline))

(defmethod ig/init-key ::inline
  [_ value]
  value)

(derive ::inline ::cb/config)

(defmethod cb/typed-block-transform [::cb/config :single-edn-file]
  [block-key system+profile ig-config]
  (let [ref-keyword       (cb/ref-keyword block-key)
        file-resource-key [::single-edn-file ref-keyword]]
    (cb/substitute-on-vec (assoc ig-config file-resource-key (-> system+profile block-key))
                          block-key ::single-edn-file-value
                          (fn [_ config]
                         (assoc config ::loaded-file (ig/ref file-resource-key))))))

(defmethod ig/init-key ::single-edn-file
  [_ {:keys [file]}]
  (edn/read-string (slurp file)))

(defmethod ig/init-key ::single-edn-file-value
  [_ {:keys [::loaded-file block-name parameter-name]}]
  (-> loaded-file block-name parameter-name))

(derive ::single-edn-file-value ::cb/config)

;; Secrets just derive from the main block

(cb/alias-block! ::cb/secret ::cb/config)