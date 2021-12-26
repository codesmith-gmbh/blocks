(ns codesmith.blocks-test
  (:require [clojure.test :refer :all]
            [codesmith.blocks :as cb]
            [integrant.core :as ig]))

(defmethod ig/init-key ::test
  [_ value]
  value)

(cb/set-same-block! ::test2 ::test)

(defmethod cb/block-transform ::all
  [block-key spec+profile ig-config]
  (assoc ig-config block-key {:test  (ig/ref ::test)
                              :test2 (ig/ref ::test2)}))

(defmethod ig/init-key ::all
  [_ value]
  value)

(def spec {:application :test
           :blocks      [::test
                         ::test2
                         ::all]})

(deftest typed-derived
  (let [profile {:environment :test
                 ::test       {:type :variant1}
                 ::test2      {:type :variant2}}
        system  (cb/init spec profile)]
    (is (= :variant1 (-> system ::test :type)))
    (is (= :variant2 (-> system ::test2 :type)))
    (is (= :variant1 (-> system ::all :test :type)))
    (is (= :variant2 (-> system ::all :test2 :type)))))
