(ns rembo.content-management
  (:use [rembo.core]
        [rembo.persistence]))

(defn- get-current-time []
  (int (/ (System/currentTimeMillis) 1000)))

(defn message-create
  "Creates a new message"
  [user-id auth-token message parent-message-id anonymous]
  (if-authorized? user-id auth-token
                  (let [message-id (next-id (retrieve :last-message-id))
                        created (get-current-time)
                        to-store {:message message 
                                  :created created
                                  :updated created
                                  :author (str user-id)
                                  :parent parent-message-id
                                  :visible true
                                  :anonymous anonymous}]
                    (do
                      (persist :last-message-id message-id)
                      (add-to-set (con parent-message-id :children) message-id)
                      (doseq [[k v] to-store]
                        (persist :messages (con message-id k) v))
                      (success message-id)))))

(defn message-retrieve
  "Retrieves message infromation"
  [message-id]
  (if (retrieve :messages (con message-id :message))
    (let [info (reduce 
                 #(assoc %1 %2 (retrieve :messages (con message-id %2))) 
                 {} 
                 [:message :parent :created :updated :author :visible
                  :anonymous])
          info (if (Boolean/valueOf (info :anonymous)) 
                 (dissoc info :author) info)
          visible (info :visible)
          info (merge (dissoc (dissoc info :anonymous) :visible)
                      { :children (retrieve-set (con message-id :children))
                        :upvotes (retrieve-set (con message-id :upvotes))})]
      (if (Boolean/valueOf visible) 
        (success info)
        (error "message is hidden")))
    (error "message doesn't exist")))

(defn message-update
  "Update message content or visibility"
  [message-id user-id auth-token {:keys [message visible]}]
  (if-authorized? user-id auth-token
                  (let [to-store {:message message
                                  :visible visible
                                  :updated (get-current-time)}]
                    (do
                      (doseq [[k v] to-store]
                        (when (not (= nil v))
                          (persist :messages (con message-id k) v)))
                      (success)))))

(defn message-upvote
  "Upvotes the message"
  [message-id user-id auth-token]
  (if-authorized? user-id auth-token
                  (do
                    (add-to-set (con message-id :upvotes) user-id)
                    (success))))
