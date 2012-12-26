(ns Rembo.test.content-management
  (:use [Rembo.content-management]
        [Rembo.user-management]
        [Rembo.persistence]
        [Rembo.test.core]
        [clojure.test]))

(deftest content-management
         (testing "message creation"
                  (user-create "cm" "asdasd" "" "")
                  (def auth (user-authenticate "cm" "asdasd"))
                  (def message-id
                    (message-create 0 auth "test message" :MAIN-PAGE false))
                  (is (= 0 message-id))
                  (def message (message-retrieve 0))
                  (is (= (message :message) "test message"))
                  (is (= (message :parent) "MAIN-PAGE"))
                  (is (= (message :created) (message :updated))) 
                  (is (= (message :author) "0"))

                  ))
