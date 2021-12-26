(ns codesmith.blocks.config-test
  (:require [clojure.test :refer :all]
            [codesmith.blocks :as cb]
            [codesmith.blocks.config :as cbc]
            [integrant.core :as ig]))

(derive ::test ::cbc/configured)

(defmethod ig/init-key
  ::test [service-key {:keys [config]}]
  (config service-key))

(derive ::test2 ::test)

(def spec {:appliction :test
           :blocks     [::cbc/config
                        ::test
                        ::test2]})

(deftest inline-config-test
  (let [test-config  {:a 1}
        test2-config {:b 2}
        profile      {:environment :test
                      ::cbc/config {:type   :inline
                                    ::test  test-config
                                    ::test2 test2-config}}
        system       (cb/init spec profile)]
    (is (= test-config (::test system)))
    (is (= test2-config (::test2 system)))))