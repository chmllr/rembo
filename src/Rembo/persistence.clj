(ns Rembo.persistence
  (:use [Rembo.settings])
  (:require [clj-redis.client :as redis]))

; Initialize the data base 
(def db 
  (redis/init
      {:url (get-setting :db-url)}))

; DB hierarchy levels
(def users "users")
(def messages "messages")

(defn retrieve 
  ([key] 0)
  ([hierarchy key]) )

(defn persist [hierarchy key value] )
