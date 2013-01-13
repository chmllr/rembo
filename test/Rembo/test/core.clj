(ns Rembo.test.core
  (:use [Rembo.core]
        [Rembo.persistence])
  (:use [clojure.test]))

(defn clean-database-fixture [f]
  (flush-database)
  (f)
  (flush-database))

(use-fixtures :once clean-database-fixture)

(defmacro isnot [value]
  `(is (not ~value)))
