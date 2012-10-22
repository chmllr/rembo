(ns Rembo.test.core
  (:use [Rembo.core]
        [Rembo.persistence])
  (:use [clojure.test]))

(defn clean-datebase-fixture [f]
  (flush-database)
  (f)
  (flush-database))

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

(deftest user-management
  (testing "user creation"
         (user-create "elrodeo" "asdasd" "I'm nothing" "asd@asd.de")
          (is (= "0" (retrieve :name2id "elrodeo")))
          (is (= "elrodeo" (retrieve :users "0:name")))
          (is (= "I'm nothing" (retrieve :users "0:about")))
          (is (= "asd@asd.de" (retrieve :users "0:email")))))
