(ns Rembo.user-management
  (:require [digest])
  (:use [Rembo.core]
        [Rembo.persistence]))

(defn user-create
  "Creates a new user"
  [name password about email]
  (let [user-id (next-id (retrieve :last-user-id))
        password (digest/md5 password)
        to-store {:name name :password password :about about :email email}]
    (do
      (persist :last-user-id user-id)
      (persist :name2id name user-id)
      (doseq [[k v] to-store]
        (persist :users (con user-id k) v)))))

(defn user-retrieve
  "Returns user info"
  [user-id]
  (reduce 
    #(assoc %1 %2 (retrieve :users (con user-id %2))) 
    {} 
    [:name :about :email]))

(defn user-authenticate
  "Authenticates the user"
  [name password]
  (let [user-id (retrieve :name2id name)
        password (digest/md5 password)
        stored-password (retrieve :users (con user-id :password))]
    (when (= password stored-password)
      (get-session user-id))))

(defn user-update
  "Update user info"
  [user-id auth-token {:keys [name password about email]}]
  (when (authorized? user-id auth-token)
          ;hash passwd if it's gonna change
    (let [password (when password (digest/md5 password)) 
          to-store {:name name :password password :about about :email email}]
      (do
        ; when a name will be changed, adust the name->id mapping as well!
        (when (to-store :name)
          (do 
            (delete :name2id ((user-retrieve user-id) :name))
            (persist :name2id name user-id)))
        (doseq [[k v] to-store]
          (when v
            (persist :users (con user-id k) v)))))))

(defn user-meta-retrieve
  "Retrieves information about the user"
  []
  (throw (Exception. "Not implemented")))
