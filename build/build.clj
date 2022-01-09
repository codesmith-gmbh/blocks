(ns build
  (:require [ch.codesmith.anvil.shell :as sh]
            [ch.codesmith.anvil.libs :as libs]
            [ch.codesmith.anvil.release :as rel]
            [clojure.tools.build.api :as b]))

(def lib 'ch.codesmith/block)
(def version (str "0.3." (b/git-count-revs {})))
(def release-branch-name "main")


(defn run-tests []
  (sh/sh! "/bin/kaocha"))

(defn jar [_]
  (libs/jar {:lib        lib
             :version    version
             :target-dir "target"
             :with-pom?  true
             :clean      true}))

(defn release [_]
  (rel/check-released-allowed release-branch-name)
  (run-tests)
  (let [jar-file (jar {})]
    (libs/deploy {:jar-file jar-file
                  :lib      lib})
    (rel/git-release! {:deps/coord          lib
                       :version             version
                       :release-branch-name release-branch-name})))

