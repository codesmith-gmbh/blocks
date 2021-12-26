(ns stan
  (:require [codesmith.blocks.config-test :as cbct]
            [codesmith.blocks-test :as cbt]
            [codesmith.blocks :as cb]
            [codesmith.blocks.config :as cbc]))


(comment

  (let [test-config  {:a 1}
        test2-config {:b 2}
        profile      {:environment :test
                      ::cbc/config {:type        :aero
                                    ::cbct/test  test-config
                                    ::cbct/test2 test2-config}}]
    (cb/system->ig cbct/spec profile))

  (let [profile {:environment :test
                 ::cbt/test {:type :variant1}
                 ::cbt/test2 {:type :variant2}}]
    (cb/system->ig cbt/spec profile))

  )