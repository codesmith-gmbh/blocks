(ns stan
  (:require [codesmith.blocks :as cb]
            [codesmith.blocks.config :as cbc]
            [integrant.repl :as ir]
            [integrant.repl.state :as irs]
            [integrant.core :as ig]))

(def system {:application :timeflow
             :blocks      [::cbc/config
                           ::config-block]})

(def profile-inline {:environment   :dev
                     ::config-block {}
                     ::cbc/config   {:type   :inline
                                     :config {:hello {:config 1}}}})

(def profile-file {:environment   :dev
                   ::config-block {}
                   ::cbc/config   {:type :single-edn-file
                                   :file "./dev/test.edn"}})

(defmethod ig/init-key ::config-block
  [_ config]
  config)

(defmethod cb/block-transform ::config-block
  [_ system+profile ig-config final-substitution]
  [(assoc ig-config ::config-block (ig/ref [::cbc/config ::config])
                    [::cbc/config ::config] {:block-name     :hello
                                             :parameter-name :config})
   final-substitution])

(ir/set-prep! (constantly (cb/system->ig system profile-file)))

(comment

  (cb/system->ig system profile-inline)

  (ir/go)
  irs/system

  )