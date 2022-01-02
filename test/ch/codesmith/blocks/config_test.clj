(ns ch.codesmith.blocks.config-test
  (:require [ch.codesmith.blocks :as cb]
            [ch.codesmith.blocks.config :as cbc]
            [clojure.test :refer :all]
            [integrant.core :as ig]
            [clojure.java.io :as io]))

(derive ::test ::cbc/configured)

(defmethod ig/init-key
  ::test [service-key {:keys [config]}]
  (config service-key))

(derive ::test1 ::test)
(derive ::test2 ::test)

(def spec {:appliction :test
           :blocks     [::cbc/config
                        ::test1
                        ::test2]})

(def test1-config {:a 1})
(def test2-config {:b 2})

(defn config-test [config-config expected-test2]
  (let [profile {:environment :test
                 ::cbc/config config-config}
        system  (cb/init spec profile)]
    (is (= test1-config (::test1 system)))
    (is (= expected-test2 (::test2 system)))))

(deftest inline-config-correctness
  (config-test {:type   :inline
                ::test1 test1-config
                ::test2 test2-config}
               test2-config))

(deftest aero-config-correctness
  (config-test {:type :aero} test2-config)
  (config-test {:type :aero
                :file (io/file "test/config2.edn")}
               {:b 3})
  (config-test {:type :aero
                :file (io/file "test/config3.edn")}
               test2-config))

(deftest aero-config-completeness
  (is
    (thrown?
      Exception
      (config-test {:type     :aero
                    :resource "config4.edn"}
                   test2-config))))