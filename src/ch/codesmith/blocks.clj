(ns ch.codesmith.blocks
  (:require [integrant.core :as ig]))

(defmulti block-transform (fn #_{:clj-kondo/ignore [:unused-binding]} [block-key spec+profile ig-config]
                            block-key))

(defmulti typed-block-transform (fn #_{:clj-kondo/ignore [:unused-binding]} [block-key spec+profile ig-config]
                                  [block-key (-> spec+profile block-key :type)]))

(defmethod block-transform :default
  [block-key spec+profile ig-config]
  (typed-block-transform block-key spec+profile ig-config))

(defn assoc-from-spec+profile [ig-config block-key spec+profile]
  (assoc ig-config block-key (spec+profile block-key)))

(defmethod typed-block-transform :default
  [block-key spec+profile ig-config]
  (assoc-from-spec+profile ig-config block-key spec+profile))

;; to integrant

(defn reduce-ig [{:keys [blocks] :as spec+profile} ig-config]
  (reduce (fn [ig-config block]
            (block-transform block spec+profile ig-config))
          ig-config
          blocks))

(defn system->ig [system profile]
  (let [spec+profile (merge system profile)]
    (reduce-ig spec+profile {})))

;; Init

(defn init [system profile]
  (-> (system->ig system profile)
      ig/prep
      ig/init))
