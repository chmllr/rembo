(ns rembo.test.core
  (:use [rembo.core]
        [rembo.persistence]
        [clojure.test]))

(defn clean-database-fixture [f]
  (flush-database)
  (f)
  (flush-database))

(use-fixtures :once clean-database-fixture)

(defmacro isnot [value]
  `(is (not ~value)))
