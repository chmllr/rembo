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
    (user-authenticate name passwd)))

(defn- create-testmessage 
  ([params]
  (create-testmessage params false))
  ([{:keys [user-id auth-token]} anonymously]
  (message-create user-id auth-token "test message" :MAIN-PAGE anonymously)))

(deftest content-management
         (testing "message creation"
                  (is (= 0 (create-testmessage (create-testuser "cm" "asdasd"))))
                  (def message (message-retrieve 0))
                  (is (= (message :message) "test message"))
                  (is (= (message :parent) "MAIN-PAGE"))
                  (is (= (message :created) (message :updated))) 
                  (is (= (message :author) "0"))
                  (is (= 1 (create-testmessage (create-testuser "hackr" "passwd") true)))
                  (def message (message-retrieve 1))
                  (is (= (message :author) nil)))
         (testing "message nesting"
                  (def new-message-id (create-testmessage (create-testuser "user" "asdasd")))
                  (is (= (inc new-message-id) (create-testmessage (create-testuser "user2" "asdasd"))))
                  (is (= (inc (inc new-message-id)) (create-testmessage (create-testuser "user3" "asdasd"))))
                  (def main-page-children (retrieve-set (con :MAIN-PAGE :children)))
                  (is (let [IDs (set (map #(str (+ new-message-id %)) [0 1 2]))]
                        (empty? (difference IDs main-page-children)))))
         (testing "message update"
                  (def message-id (create-testmessage 
                                    (create-testuser "cm2" "asdasd")))
                  (def user-info (user-authenticate "cm2" "asdasd"))
                  (def creation-date (:created (message-retrieve message-id)))
                  (is (= creation-date (:updated (message-retrieve message-id))))
                  ; we need to delay the update by >1sec s.t. the :updated != :created
                  (. Thread (sleep 1100))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:message "hello world"})
                  (def message (message-retrieve message-id))
                  (isnot (= (message :message) "test message"))
                  (is (= (message :message) "hello world"))
                  (isnot (= creation-date (:updated message)))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:message "goodbye world"})
                  (def message (message-retrieve message-id))
                  (is (= (message :message) "goodbye world"))
                  (message-update message-id (user-info :user-id)
                                  "wrong token"
                                  {:message "empty"})
                  (def message (message-retrieve message-id))
                  (isnot (= (message :message) "empty"))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:visible false})
                  (def message (message-retrieve message-id))
                  (is (= nil message))
                  (message-update message-id (user-info :user-id)
                                  (user-info :auth-token)
                                  {:visible true})
                  (def message (message-retrieve message-id))
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
                  (message-upvote message-id (user-info3 :user-id) "wrongpass")
                  (def message (message-retrieve message-id))
                  (isnot (contains? (message :upvotes) (user-info2 :user-id)))
                  (message-upvote message-id (user-info2 :user-id)
                                  (user-info2 :auth-token))
                  (def message (message-retrieve message-id))
                  (is (contains? (message :upvotes) (user-info2 :user-id)))
                  (isnot (contains? (message :upvotes) (user-info3 :user-id)))))
