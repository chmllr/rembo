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

(defn authorized?
  "Checks the correctness of given session token"
  [user-id auth-token]
  (= auth-token (get-session user-id)))

(defn next-id
  "Generates next id by incrementation"
  [id]
  (if id (inc (Integer/parseInt id)) 0))

(defn success
  "Return result when successful execution"
  [result]
  {:status :ok :result result})

(defn error
  "Return error when failed execution"
  [message]
  {:status :failed :message message})
