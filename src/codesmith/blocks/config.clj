(ns codesmith.blocks.config
  (:require [codesmith.blocks :as cb]
            [integrant.core :as ig]
            [clojure.edn :as edn]))

(defn- compute-inline-substitution [ig-config final-substitution system+profile block-key inline-block-key]
  (cb/compute-substitution ig-config final-substitution
                           block-key inline-block-key
                           (fn [_ config]
                             (-> system+profile block-key :config
                                 (get (:block-name config))
                                 (get (:parameter-name config))))))

(defmethod cb/typed-block-transform [::config :inline]
  [block-key system+profile ig-config final-substitution]
  [ig-config (compute-inline-substitution ig-config final-substitution system+profile
                                          block-key ::inline)])

(defmethod ig/init-key ::inline
  [_ value]
  value)

(derive ::inline ::config)

(defmethod cb/typed-block-transform [::config :single-edn-file]
  [block-key system+profile ig-config final-substitution]
  (let [ref-keyword       (cb/ref-keyword block-key)
        file-resource-key [::single-edn-file ref-keyword]
        ig-config         (assoc ig-config
                            file-resource-key (-> system+profile block-key))]
    [ig-config (cb/compute-substitution ig-config
                                        final-substitution
                                        block-key ::double-map-value
                                        (fn [_ config] config
                                          (assoc config :loaded-file (ig/ref file-resource-key))))]))

(defmethod ig/init-key ::single-edn-file
  [_ {:keys [file]}]
  (edn/read-string (slurp file)))

(defmethod ig/init-key ::double-map-value
  [_ {:keys [loaded-file block-name parameter-name]}]
  (-> loaded-file
      (get block-name)
      (get parameter-name)))

(derive ::double-map-value ::config)

;; Secrets just derive from the main block

(cb/alias-block! ::secret ::config)