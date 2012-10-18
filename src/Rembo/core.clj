(ns Rembo.core
  (:require [digest])
  (:use [Rembo.settings]
        [Rembo.persistence]))

(defn- con [id key]
  (str id ":" key))

(defn- get-session [user-id]
  (let [password (retrieve :users (con user-id :password))
        salt (get-setting :salt)]
    (digest/md5 (str password salt))))

(defn user-create
  "Creates a new user"
  [name password about email]
  (let [user-id (inc (retrieve :last-user-id))
        to-store (hash-map :name name :password password :about about :email email)]
    (doseq [[k v] to-store]
      (persist :users (con user-id k) v))))

(defn user-authenticate
  "Authenticates the user"
  [name password]
  (let [user-id (retrieve :name2id name)
        stored-password (retrieve :users (con user-id :password))]
    (when (= password stored-password)
      (get-session user-id))))
      

(defn user-update
  "Update user info"
  [])
