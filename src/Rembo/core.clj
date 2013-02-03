(ns Rembo.core
  (:require [digest]
            [cheshire.core :refer :all])
  (:use [Rembo.persistence]
        [Rembo.settings]))

(defn con
  "Concatenation helper"
  [id key]
  (str id ":" (name key)))

(defn get-session
  "Retrieves user data and creates a hash based a salt and user password"
  [user-id]
  (let [password (retrieve :users (con user-id :password))
        salt (get-setting :salt)]
    (digest/md5 (str password salt))))

(defmacro if-authorized?
  "Checks the correctness of given session token and 
  executes action or throws an error"
  [user-id auth-token action]
  `(if (= ~auth-token (get-session ~user-id))
     ~action
     (error "unauthorized access")))

(defn next-id
  "Generates next id by incrementation"
  [id]
  (if id (inc (Integer/parseInt id)) 0))

(defn success
  "Return result when successful execution"
  ([] {:status :ok})
  ([result]
   (assoc (success) :result result)))

(defn error
  "Return error when failed execution"
  [message]
  {:status :failed :message message})
