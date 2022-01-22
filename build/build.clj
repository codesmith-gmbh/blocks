(ns build
  (:require [ch.codesmith.anvil.shell :as sh]
            [ch.codesmith.anvil.libs :as libs]
            [ch.codesmith.anvil.release :as rel]
            [clojure.tools.build.api :as b]))

(def lib 'ch.codesmith/blocks)
(def version (str "0.3." (b/git-count-revs {})))
(def release-branch-name "master")

(def description-data
  {:license        :epl
   :inception-year 2020
   :description    "Utils on top of integrant"
   :gh-project     "codesmith-gmbh/blocks"
   :org-name       "Codesmith GmbH"
   :authors        ["Stanislas Nanchen"]
   :org-url        "https://codesmith.ch"})


(defn run-tests []
  (sh/sh! "./bin/kaocha"))

(defn jar [_]
  (libs/jar {:lib              lib
             :version          version
             :target-dir       "target"
             :with-pom?        true
             :description-data description-data
             :clean?           true}))

(defn release [_]
  (rel/check-released-allowed release-branch-name)
  (run-tests)
  (let [jar-file (jar {})]
    (libs/deploy {:jar-file jar-file
                  :lib      lib
                  :pom-file "target/classes/META-INF/maven/ch.codesmith/blocks/pom.xml"})
    (rel/git-release! {:deps/coord          lib
                       :version             version
                       :release-branch-name release-branch-name
                       :artifact-type       :mvn})))

