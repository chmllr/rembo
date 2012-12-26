(ns Rembo.core
  (:require [digest])
  (:use [Rembo.persistence]
        [Rembo.settings]))

(defn con [id key]
  (str id ":" (name key)))

(defn get-session [user-id]
  (let [password (retrieve :users (con user-id :password))
        salt (get-setting :salt)]
    (digest/md5 (str password salt))))

(defn authorized? [user-id auth]
  (= auth (get-session user-id)))

(defn next-id [id]
  (if id (inc (Integer/parseInt id)) 0))
