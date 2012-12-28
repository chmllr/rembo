(ns Rembo.test.user-management
  (:use [Rembo.user-management]
        [Rembo.persistence]
        [Rembo.test.core]
        [clojure.test]))

(use-fixtures :once clean-datebase-fixture)

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
                  (def authenticated-user (user-authenticate "cm" "12345"))
                  (def auth (authenticated-user :auth-token))
                  (def user-id (authenticated-user :user-id))
                  (is (= "1" user-id))
                  (isnot (= nil auth))
                  (user-update user-id auth {:name "lol"})
                  (is (= "lol" ((user-retrieve user-id) :name)))
                  (user-update user-id auth {:name "chris"
                                       :about "just a user"
                                       :email "chr@is.com"})
                  (is (= "chris" ((user-retrieve user-id) :name)))
                  (is (= "just a user" ((user-retrieve user-id) :about)))
                  (is (= "chr@is.com" ((user-retrieve user-id) :email)))
                  (user-update user-id auth {:password "lalala" :name "mllr"})
                  (is (= "mllr" ((user-retrieve user-id) :name)))
                  (user-update user-id auth {:name "chris"})
                  (isnot (= "chris" ((user-retrieve user-id) :name)))
                  (def auth (:auth-token (user-authenticate "mllr" "lalala")))
                  (isnot (= nil auth))
                  (user-update user-id auth {:name "chris"})
                  (is (= "chris" ((user-retrieve user-id) :name)))))
