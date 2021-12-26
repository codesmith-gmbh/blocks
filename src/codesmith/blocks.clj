(ns codesmith.blocks
  (:require [integrant.core :as ig])
  (:import (clojure.lang MultiFn)))

(defmulti block-transform (fn [block-key spec+profile ig-config]
                            block-key))

(defmulti typed-block-transform (fn [block-key spec+profile ig-config]
                                  [block-key (-> spec+profile block-key :type)]))

(defmethod block-transform :default
  [block-key spec+profile ig-config]
  (typed-block-transform block-key spec+profile ig-config))

(defn assoc-from-spec+profile [ig-config block-key spec+profile]
  (assoc ig-config block-key (spec+profile block-key)))

(defmethod typed-block-transform :default
  [block-key spec+profile ig-config]
  (assoc-from-spec+profile ig-config block-key spec+profile))

;; duplicate blocks

(defn add-method [^MultiFn multifn dispatch-val fn]
  (.addMethod multifn dispatch-val fn))

(defn set-same-method! [^MultiFn multifn dispatch-val-dst dispatch-val-src]
  (if-let [method (get-method multifn dispatch-val-src)]
    (if (not= method (get-method multifn :default))
      (add-method multifn dispatch-val-dst method))))

(defn set-same-methods! [multifns dispatch-val-dst dispatch-val-src]
  (doseq [multifn multifns]
    (set-same-method! multifn dispatch-val-dst dispatch-val-src)))

(defn set-same-block-transform! [dispatch-val-dst dispatch-val-src]
  (set-same-methods! [block-transform typed-block-transform]
                     dispatch-val-dst
                     dispatch-val-src))

(defn set-same-block! [dispatch-val-dst dispatch-val-src]
  (set-same-block-transform! dispatch-val-dst dispatch-val-src)
  (set-same-methods! [ig/init-key ig/halt-key!
                      ig/prep-key ig/resolve-key
                      ig/resume-key ig/suspend-key!]
                     dispatch-val-dst
                     dispatch-val-src))

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
