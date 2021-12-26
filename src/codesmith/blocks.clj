(ns codesmith.blocks
  (:require [integrant.core :as ig]))

(defmulti block-transform (fn [block-key system+profile ig-config]
                                   block-key))

(defmulti typed-block-transform (fn [block-key system+profile ig-config]
                                         [block-key (-> system+profile block-key :type)]))

(defmethod block-transform :default
  [block-key system+profile ig-config]
  (typed-block-transform block-key system+profile ig-config))

;; to integrant

(defn reduce-ig [{:keys [blocks] :as system+profile} ig-config]
  (reduce (fn [ig-config block]
            (block-transform block system+profile ig-config))
          ig-config
          blocks))

(defn system->ig [system profile]
  (let [system+profile (merge system profile)]
    (reduce-ig system+profile {})))

;; Init

(defn init [system profile]
  (-> (system->ig system profile)
      ig/prep
      ig/init))
