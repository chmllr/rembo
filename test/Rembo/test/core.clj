(ns Rembo.test.core
  (:use [Rembo.core]
        [Rembo.persistence])
  (:use [clojure.test]))

(defn clean-datebase-fixture [f]
  (flush-database)
  (f)
  (flush-database))

(use-fixtures :once clean-datebase-fixture)

(defmacro isnot [value]
  `(is (not ~value)))
