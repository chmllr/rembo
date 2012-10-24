(ns Rembo.test.core
  (:use [Rembo.core]
        [Rembo.persistence])
  (:use [clojure.test]))

(defn clean-datebase-fixture [f]
  (flush-database)
  (f)
  #_(flush-database))

(use-fixtures :once clean-datebase-fixture)

(defmacro isnot [value]
  `(is (not ~value)))

(deftest user-management
         (testing "user creation"
                  (user-create "elrodeo" "asdasd" "I'm nothing" "asd@asd.de")
                  (is (= "0" (retrieve :name2id "elrodeo")))
                  (is (= "elrodeo" (retrieve :users "0:name")))
                  (is (= "I'm nothing" (retrieve :users "0:about")))
                  (is (= "asd@asd.de" (retrieve :users "0:email"))))
         (testing "user authentication"
                  (is (= nil (user-authenticate "elrodeo" "asdasdasd")))
                  (isnot (= nil (user-authenticate "elrodeo" "asdasd"))))
         (testing "user info update"
                  (user-create "cm" "12345" "software engineer" "cm@asd.de")
                  (def auth (user-authenticate "cm" "12345"))
                  (isnot (= nil auth))
                  (user-update 1 auth {:name "lol"})
                  (is (= "lol" (retrieve :users "1:name")))
                  (user-update 1 auth {:name "chris" :about "just a user" :email "chr@is.com"})
                  (is (= "chris" (retrieve :users "1:name")))
                  (is (= "just a user" (retrieve :users "1:about")))
                  (is (= "chr@is.com" (retrieve :users "1:email")))
                  (user-update 1 auth {:password "lalala" :name "mllr"})
                  (is (= "mllr" (retrieve :users "1:name")))
                  (user-update 1 auth {:name "chris"})
                  (isnot (= "chris" (retrieve :users "1:name")))
                  (def auth (user-authenticate "mllr" "lalala"))
                  (isnot (= nil auth))
                  (user-update 1 auth {:name "chris"})
                  (is (= "chris" (retrieve :users "1:name")))))
