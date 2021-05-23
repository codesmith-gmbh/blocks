(ns codesmith.blocks
  (:require [integrant.core :as ig]))

;; Utils

(defn substitute-on-vec [m first-key substitute-first-key f]
  (into {}
        (map (fn [[key value :as entry]]
               (if (and (vector? key)
                        (= (first key) first-key))
                 (let [second-key (second key)]
                   [[substitute-first-key second-key] (f second-key value)])
                 entry)))
        m))

(defmacro assoc-if-absent [m key value]
  `(let [m#   ~m
         key# ~key]
     (if (contains? m# key#)
       m#
       (assoc m# key# ~value))))

(defn ref-keyword [key]
  (keyword "ref"
           (str (namespace key) "." (name key))))

;; Blocks

(defonce block-hierarchy (make-hierarchy))

(defmulti block-transform (fn [block-key system+profile ig-config]
                            block-key)
          :hierarchy #'block-hierarchy)

(defmulti typed-block-transform (fn [block-key system+profile ig-config]
                                  [block-key (-> system+profile block-key :type)])
          :hierarchy #'block-hierarchy)

(defmethod block-transform :default
  [block-key system+profile ig-config]
  (typed-block-transform block-key system+profile ig-config))

(defmacro alias-block! [child-block parent-block]
  `(let [child-block#  ~child-block
         parent-block# ~parent-block]
     (derive parent-block# child-block#)
     (alter-var-root #'block-hierarchy derive child-block# parent-block#)))

;; to integrant

(defn reduce-ig [{:keys [blocks] :as system+profile} ig-config]
  (reduce (fn [ig-config block]
            (block-transform block system+profile ig-config))
          ig-config
          blocks))

(def max-reductions 100)

(defn system->ig [system profile]
  (let [system+profile (merge system profile)]
    (loop [i         0
           ig-config (reduce-ig system+profile {})]
      (when (>= i max-reductions)
        (throw (IllegalStateException. "Could not reduce")))
      (let [next-ig-config (reduce-ig system+profile ig-config)]
        (if (= next-ig-config ig-config)
          ig-config
          (recur (inc i)
                 next-ig-config))))))

;; Init

(defn init [system profile]
  (-> (system->ig system profile)
      ig/prep
      ig/init))
