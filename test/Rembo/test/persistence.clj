(ns Rembo.test.core
  (:use [Rembo.persistence])
  (:use [clojure.test]))

(use-fixtures :once clean-datebase-fixture)

(deftest persistence
         (testing "persistence layer"
                  (is (= nil (retrieve :test)))
                  (persist :test "lol")
                  (is (= "lol" (retrieve :test)))
                  (is (= nil (retrieve :hierarchy :test)))
                  (persist :hierarchy :test "haha")
                  (is (= "haha" (retrieve :hierarchy :test)))
                  (delete :test)
                  (delete :hierarchy :test)
                  (is (= nil (retrieve :test)))
                  (is (= nil (retrieve :hierarchy :test)))))
