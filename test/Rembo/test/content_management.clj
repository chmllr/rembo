(ns Rembo.test.content-management
  (:use [Rembo.content-management]
        [Rembo.user-management]
        [Rembo.persistence]
        [Rembo.test.core]
        [clojure.test]))

(use-fixtures :once clean-datebase-fixture)

(defn- create-testuser []
  (do
    (user-create "cm" "asdasd" "" "")
    (user-authenticate "cm" "asdasd")))

(defn- create-testmessage [{:keys [user-id auth-token]}]
  (message-create user-id auth-token "test message" :MAIN-PAGE false))

(deftest content-management
         (testing "message creation"
                  (is (= 0 (create-testmessage (create-testuser))))
                  (def message (message-retrieve 0))
                  (is (= (message :message) "test message"))
                  (is (= (message :parent) "MAIN-PAGE"))
                  (is (= (message :created) (message :updated))) 
                  (is (= (message :author) "0")))
         (testing "message update"
                  (def message-id (create-testmessage (create-testuser)))
                  (def user-info (user-authenticate "cm" "asdasd"))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:message "hello world"})
                  (def message (message-retrieve 1))
                  (isnot (= (message :message) "test message"))
                  (is (= (message :message) "hello world"))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:message "goodbye world"})
                  (def message (message-retrieve 1))
                  (is (= (message :message) "goodbye world"))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:visible false})
                  (def message2 (message-retrieve 1))
                  (is (= nil message2))))
