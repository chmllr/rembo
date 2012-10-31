(ns Rembo.test.user-management
  (:use [Rembo.user-management]
        [Rembo.persistence]
        [Rembo.test.core]
        [clojure.test]))

(deftest user-management
         (testing "user creation"
                  (user-create "elrodeo" "asdasd" "I'm nothing" "asd@asd.de")
                  (is (= "0" (retrieve :name2id "elrodeo")))
                  (is (= "elrodeo" ((user-retrieve 0) :name)))
                  (is (= "I'm nothing" ((user-retrieve 0) :about)))
                  (is (= "asd@asd.de" ((user-retrieve 0) :email))))
         (testing "user authentication"
                  (is (= nil (user-authenticate "elrodeo" "asdasdasd")))
                  (isnot (= nil (user-authenticate "elrodeo" "asdasd"))))
         (testing "user info update"
                  (user-create "cm" "12345" "software engineer" "cm@asd.de")
                  (def auth (user-authenticate "cm" "12345"))
                  (isnot (= nil auth))
                  (user-update 1 auth {:name "lol"})
                  (is (= "lol" ((user-retrieve 1) :name)))
                  (user-update 1 auth {:name "chris" :about "just a user" :email "chr@is.com"})
                  (is (= "chris" ((user-retrieve 1) :name)))
                  (is (= "just a user" ((user-retrieve 1) :about)))
                  (is (= "chr@is.com" ((user-retrieve 1) :email)))
                  (user-update 1 auth {:password "lalala" :name "mllr"})
                  (is (= "mllr" ((user-retrieve 1) :name)))
                  (user-update 1 auth {:name "chris"})
                  (isnot (= "chris" ((user-retrieve 1) :name)))
                  (def auth (user-authenticate "mllr" "lalala"))
                  (isnot (= nil auth))
                  (user-update 1 auth {:name "chris"})
                  (is (= "chris" ((user-retrieve 1) :name)))))
