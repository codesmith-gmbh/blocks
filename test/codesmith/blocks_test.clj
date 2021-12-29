(ns codesmith.blocks-test
  (:require [clojure.test :refer :all]
            [codesmith.blocks :as cb]
            [integrant.core :as ig]))

(defmethod ig/init-key ::test
  [_ value]
  value)

(derive ::test1 ::test)
(derive ::test2 ::test)

(defmethod cb/block-transform ::all
  [block-key spec+profile ig-config]
  (assoc ig-config block-key {:test1 (ig/ref ::test1)
                              :test2 (ig/ref ::test2)}))

(defmethod ig/init-key ::all
  [_ value]
  value)

(def spec {:application :test
           :blocks      [::test1
                         ::test2
                         ::all]})

(deftest typed-derived
  (let [profile {:environment :test
                 ::test1      {:type :variant1}
                 ::test2      {:type :variant2}}
        system  (cb/init spec profile)]
    (is (= :variant1 (-> system ::test1 :type)))
    (is (= :variant2 (-> system ::test2 :type)))
    (is (= :variant1 (-> system ::all :test1 :type)))
    (is (= :variant2 (-> system ::all :test2 :type)))))
