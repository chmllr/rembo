(ns Rembo.test.content-management
  (:use [Rembo.content-management]
        [Rembo.user-management]
        [Rembo.persistence]
        [Rembo.test.core]
        [clojure.test]))

(use-fixtures :once clean-datebase-fixture)

(defn- create-testuser [name passwd]
  (do
    (user-create name passwd "" "")
    (user-authenticate name passwd)))

(defn- create-testmessage [{:keys [user-id auth-token]}]
  (message-create user-id auth-token "test message" :MAIN-PAGE false))

(deftest content-management
         (testing "message creation"
                  (is (= 0 (create-testmessage (create-testuser "cm" "asdasd"))))
                  (def message (message-retrieve 0))
                  (is (= (message :message) "test message"))
                  (is (= (message :parent) "MAIN-PAGE"))
                  (is (= (message :created) (message :updated))) 
                  (is (= (message :author) "0")))
         (testing "message update"
                  (def message-id (create-testmessage 
                                    (create-testuser "cm2" "asdasd")))
                  (def user-info (user-authenticate "cm2" "asdasd"))
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
                                  "wrong token"
                                  {:message "empty"})
                  (def message (message-retrieve 1))
                  (isnot (= (message :message) "empty"))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:visible false})
                  (def message (message-retrieve 1))
                  (is (= nil message))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:visible true})
                  (def message (message-retrieve 1))
                  (is (= (message :message) "goodbye world")))
         (testing "message upvotes"
                  (def message-id (create-testmessage 
                                    (create-testuser "cm3" "asdasd")))
                  (def user-info (user-authenticate "cm" "asdasd"))
                  (def user-info2 (user-authenticate "cm2" "asdasd"))
                  (def user-info3 (user-authenticate "cm3" "asdasd"))
                  (message-upvote message-id (user-info :user-id)
                                  (user-info :auth-token))
                  (def message (message-retrieve message-id))
                  (is (contains? (message :upvotes) (user-info :user-id)))
                  (message-upvote message-id (user-info2 :user-id)
                                  (user-info2 :auth-token))
                  (def message (message-retrieve message-id))
                  (is (contains? (message :upvotes) (user-info2 :user-id)))
                  (isnot (contains? (message :upvotes) (user-info3 :user-id)))))
