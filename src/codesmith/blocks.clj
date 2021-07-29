(ns codesmith.blocks
  (:require [integrant.core :as ig]))

;; Utils

(defn compute-substitution [m substitution first-key substitute-first-key f]
  (let [result (into substitution
                     (comp
                       (keep (fn [key]
                               (if (and (vector? key)
                                        (= (first key) first-key))
                                 (let [second-key (second key)]
                                   [key {:key [substitute-first-key second-key]
                                         :f   (partial f second-key)}]))))
                       (remove #(-> % first substitution)))
                     (keys m))]
    result))

(defn substitute [m substitution]
  (into {}
        (map (fn [[key1 val :as entry]]
               (if-let [{:keys [key f]} (get substitution key1)]
                 [key (f val)]
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

(defmulti block-transform (fn [block-key system+profile ig-config final-substitution]
                            block-key)
          :hierarchy #'block-hierarchy)

(defmulti typed-block-transform (fn [block-key system+profile ig-config final-substitution]
                                  [block-key (-> system+profile block-key :type)])
          :hierarchy #'block-hierarchy)

(defmethod block-transform :default
  [block-key system+profile ig-config final-substitution]
  (typed-block-transform block-key system+profile ig-config final-substitution))

(defmacro alias-block! [child-block parent-block]
  `(let [child-block#  ~child-block
         parent-block# ~parent-block]
     (derive parent-block# child-block#)
     (alter-var-root #'block-hierarchy derive child-block# parent-block#)))

;; to integrant

(defn reduce-ig [{:keys [blocks] :as system+profile} ig-config final-substitution]
  (reduce (fn [step block]
            (block-transform block system+profile (first step) (second step)))
          [ig-config final-substitution]
          blocks))

(def max-reductions 100)

(defn system->ig [system profile]
  (let [system+profile (merge system profile)]
    (loop [i                   0
           config+substitution (reduce-ig system+profile {} {})]
      (when (>= i max-reductions)
        (throw (IllegalStateException. "Could not reduce")))
      (let [next-config+substitution (reduce-ig system+profile
                                                (first config+substitution)
                                                (second config+substitution))]
        (if (= next-config+substitution config+substitution)
          (apply substitute config+substitution)
          (recur (inc i)
                 next-config+substitution))))))

;; Init

(defn init [system profile]
  (-> (system->ig system profile)
      ig/prep
      ig/init))
