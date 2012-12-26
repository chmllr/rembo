(ns Rembo.content-management
  (:use [Rembo.core]
        [Rembo.persistence]))

(defn message-create
  "Creates a new message"
  [user-id auth-token message parent-message-id anonymous]
  (when (authorized? user-id auth-token)
    (let [message-id (next-id (retrieve :last-message-id))
          created (str (int (/ (System/currentTimeMillis) 1000)))
          to-store {:message message 
                    :created created
                    :updated created
                    :author (str user-id)
                    :parent parent-message-id
                    :visible true
                    :anonymous anonymous}]
      (do
        (persist :last-message-id message-id)
        (add-to-set (con message-id :children) message-id)
        (doseq [[k v] to-store]
          (persist :messages (con message-id k) v))
        message-id))))

(defn message-retrieve
  "Retrieves message infromation"
  [message-id]
  (let [info (reduce 
                      #(assoc %1 %2 (retrieve :messages (con message-id %2))) 
                      {} 
                      [:message :parent :created :updated :author :visible
                       :anonymous])
        info (if (info :anonymous) (dissoc info :author) info)
        info (dissoc info :author)
        visible (info :visible)
        info (dissoc info :visible)
        info (assoc info :children (retrieve (con message-id :children)))
        info (assoc info :upvotes (retrieve (con message-id :upvotes)))]
    (when visible info)))
