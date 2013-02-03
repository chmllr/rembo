(ns rembo.user-management
  (:require [digest])
  (:use [rembo.core]
        [rembo.persistence]))

(defn user-create
  "Creates a new user"
  [name password about email]
  (let [user-id (next-id (retrieve :last-user-id))
        password (digest/md5 password)
        to-store {:name name :password password :about about :email email}]
    (if (retrieve :name2id name)
      (error "user name taken")
      (do
        (persist :last-user-id user-id)
        (persist :name2id name user-id)
        (doseq [[k v] to-store]
          (persist :users (con user-id k) v))
        (success user-id)))))

(defn user-retrieve
  "Returns user info"
  [user-id]
  (if (retrieve :users (con user-id :name))
    (success
      (reduce 
        #(assoc %1 %2 (retrieve :users (con user-id %2))) 
        {} 
        [:name :about :email]))
    (error "user doesn't exist")))

(defn user-authenticate
  "Authenticates the user"
  [name password]
  (let [user-id (retrieve :name2id name)
        password (digest/md5 password)
        stored-password (retrieve :users (con user-id :password))]
    (if (= password stored-password)
      (success {:user-id user-id
                :auth-token (get-session user-id)})
      (error "username or password is wrong"))))

(defn user-update
  "Update user info"
  [user-id auth-token {:keys [name password about email]}]
  (if-authorized? user-id auth-token
                  ;hash passwd if it's gonna change
                  (let [password (when password (digest/md5 password)) 
                        to-store {:name name :password password :about about :email email}]
                    (do
                      ; when a name will be changed, adjust the name->id mapping as well!
                      (when (to-store :name)
                        (do 
                          (delete :name2id ((user-retrieve user-id) :name))
                          (persist :name2id name user-id)))
                      (doseq [[k v] to-store]
                        (when v
                          (persist :users (con user-id k) v)))
                      (success)))))

(defn user-meta-retrieve
  "Retrieves information about the user"
  [user-id]
  (error "method not implemented"))
