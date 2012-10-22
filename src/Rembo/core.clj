(ns Rembo.core
  (:require [digest])
  (:use [Rembo.settings]
        [Rembo.persistence]))

(defn- con [id key]
  (str id ":" (name key)))

(defn- get-session [user-id]
  (let [password (retrieve :users (con user-id :password))
        salt (get-setting :salt)]
    (digest/md5 (str password salt))))

(defn- authorized? [user-id auth]
  (= auth (get-session user-id)))

(defn user-create
  "Creates a new user"
  [name password about email]
  (let [user-id (retrieve :last-user-id)
        user-id (if user-id (inc user-id) 0)
        password (digest/md5 password)
        to-store (hash-map :name name :password password :about about :email email)]
    (do
      (persist :name2id name (str user-id))
      (doseq [[k v] to-store]
        (persist :users (con user-id k) v)))))

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
  [user-id auth {:keys [name password about email]}]
  (when (authorized? user-id auth)
    (let [to-store (hash-map :name name :password password :about about :email email)]
      (doseq [[k v] to-store]
        (when v
          (persist :users (con user-id k) v))))))

(defn user-retrieve
  "Returns user info"
  [user-id]
  (reduce 
    #(assoc %1 %2 (retrieve :users (con user-id %2))) 
    {} 
    [:name :about :email]))
