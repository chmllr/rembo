(ns Rembo.test.content-management
  (:use [Rembo.content-management]
        [Rembo.user-management]
        [Rembo.persistence]
        [Rembo.test.core]
        [Rembo.core]
        [clojure.set]
        [clojure.test]))

(use-fixtures :once clean-database-fixture)

(defn- create-testuser [name passwd]
  (do
    (user-create name passwd "" "")
    (:result (user-authenticate name passwd))))

(defn- create-testmessage 
  ([params]
  (create-testmessage params false))
  ([{:keys [user-id auth-token]} anonymously]
  (:result (message-create user-id auth-token "test message" :MAIN-PAGE anonymously))))

(deftest content-management
         (testing "message creation"
                  (is (= 0 (create-testmessage (create-testuser "cm" "asdasd"))))
                  (def message (:result (message-retrieve 0)))
                  (is (= (message :message) "test message"))
                  (is (= (message :parent) "MAIN-PAGE"))
                  (is (= (message :created) (message :updated))) 
                  (is (= (message :author) "0"))
                  (is (= 1 (create-testmessage (create-testuser "hackr" "passwd") true)) 
                      "create a new message anonymously")
                  (def message (:result (message-retrieve 1)))
                  (is (= (message :author) nil)) "the author shouldn't be returned")
         (testing "message nesting"
                  (def new-message-id (create-testmessage (create-testuser "user" "asdasd")))
                  (is (= (inc new-message-id) (create-testmessage (create-testuser "user2" "asdasd"))) 
                      "message id should increment")
                  (is (= (inc (inc new-message-id)) (create-testmessage (create-testuser "user3" "asdasd"))))
                  ; TODO: somth is wrong here: why not retrieving over message-retrieve?
                  (def main-page-children (retrieve-set (con :MAIN-PAGE :children)))
                  (is (let [IDs (set (map #(str (+ new-message-id %)) [0 1 2]))]
                        (empty? (difference IDs main-page-children))))
                      "the first message id and 2 consecutive ids should be all members of the children set of the root")
         (testing "message update"
                  (def message-id (create-testmessage 
                                    (create-testuser "cm2" "asdasd")))
                  (def user-info (:result (user-authenticate "cm2" "asdasd")))
                  (def message (:result (message-retrieve message-id)))
                  (def creation-date (:created message))
                  (is (= creation-date (:updated message)))
                  ; we need to delay the update by >1sec s.t. the :updated != :created
                  (. Thread (sleep 1100))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:message "hello world"})
                  (def message (:result (message-retrieve message-id)))
                  (isnot (= (message :message) "test message"))
                  (is (= (message :message) "hello world"))
                  (isnot (= creation-date (:updated message)))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:message "goodbye world"})
                  (def message (:result (message-retrieve message-id)))
                  (is (= (message :message) "goodbye world"))
                  (message-update message-id (user-info :user-id)
                                  "wrong token"
                                  {:message "empty"})
                  (def message (:result (message-retrieve message-id)))
                  (isnot (= (message :message) "empty"))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:visible false})
                  (def message (:result (message-retrieve message-id)))
                  (is (= nil message))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:visible true})
                  (def message (:result (message-retrieve message-id)))
                  (is (= (message :message) "goodbye world")))
         (testing "message upvotes"
                  (def message-id (create-testmessage 
                                    (create-testuser "cm3" "asdasd")))
                  (def user-info (:result (user-authenticate "cm" "asdasd")))
                  (def user-info2 (:result (user-authenticate "cm2" "asdasd")))
                  (def user-info3 (:result (user-authenticate "cm3" "asdasd")))
                  (message-upvote message-id (user-info :user-id)
                                  (user-info :auth-token))
                  (def message (:result (message-retrieve message-id)))
                  (is (contains? (message :upvotes) (user-info :user-id)))
                  (message-upvote message-id (user-info3 :user-id) "wrongpass")
                  (def message (:result (message-retrieve message-id)))
                  (isnot (contains? (message :upvotes) (user-info2 :user-id)))
                  (message-upvote message-id (user-info2 :user-id)
                                  (user-info2 :auth-token))
                  (def message (:result (message-retrieve message-id)))
                  (is (contains? (message :upvotes) (user-info2 :user-id)))
                  (isnot (contains? (message :upvotes) (user-info3 :user-id)))))
