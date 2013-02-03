(ns rembo.test.user-management
  (:use [rembo.user-management]
        [rembo.persistence]
        [rembo.test.core]
        [clojure.test]))

(use-fixtures :once clean-database-fixture)

(deftest user-management
         (testing "user creation"
                  (user-create "elrodeo" "asdasd" "I'm nothing" "asd@asd.de")
                  (is (= :failed (:status (user-create "elrodeo" "asdasd"
                                            "I'm nothing" "asd@asd.de"))))
                  (is (= "0" (retrieve :name2id "elrodeo")))
                  (def user (:result (user-retrieve 0)))
                  (is (= "elrodeo" (user :name)))
                  (is (= "I'm nothing" (user :about)))
                  (is (= "asd@asd.de" (user :email))))
         (testing "user authentication"
                  (is (= :failed (:status (user-authenticate "elrodeo" "asdasdasd"))))
                  (is (= :ok (:status (user-authenticate "elrodeo" "asdasd")))))
         (testing "user info update"
                  (user-create "cm" "12345" "software engineer" "cm@asd.de")
                  (def authenticated-user (:result (user-authenticate "cm" "12345")))
                  (def auth (authenticated-user :auth-token))
                  (def user-id (authenticated-user :user-id))
                  (is (= "1" user-id))
                  (isnot (= nil auth))
                  (user-update user-id auth {:name "lol"})
                  (is (= "lol" ((:result (user-retrieve user-id)) :name)))
                  (user-update user-id auth {:name "chris"
                                       :about "just a user"
                                       :email "chr@is.com"})
                  (def user (:result (user-retrieve user-id)))
                  (is (= "chris" (user :name)))
                  (is (= "just a user" (user :about)))
                  (is (= "chr@is.com" (user :email)))
                  (user-update user-id auth {:password "lalala" :name "mllr"})
                  (is (= "mllr" ((:result (user-retrieve user-id)) :name)))
                  (is (= :failed (:status (user-update user-id auth {:name "chris"}))))
                  (isnot (= "chris" ((:result (user-retrieve user-id)) :name)))
                  (def auth (:auth-token (:result (user-authenticate "mllr" "lalala"))))
                  (isnot (= nil auth))
                  (user-update user-id auth {:name "chris"})
                  (is (= "chris" ((:result (user-retrieve user-id)) :name))))
         (testing "meta information retrieval"
                  (is (= :failed (:status (user-meta-retrieve nil))))))
