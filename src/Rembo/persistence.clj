(ns rembo.persistence
  (:use [settingator.core])
  (:require [clj-redis.client :as redis]))

; Initialize the data base 
(def db 
  (redis/init
    {:url (get-setting :db-url)}))

; returns the name if the given value is a keyword,
; or just returns the string back
(defn- normalize [value]
  (str 
    (if (keyword? value)
      (name value)
      value)))

(defn retrieve 
  "Retrieves the value of the given field;
  or retrieves the value of the given field of the specified hash key.
  If the field name of hash field is a clojure keyword, its name will be used"
  ([field]
   (redis/get db (normalize field)))

  ([hkey field]
   (redis/hget db (normalize hkey) (normalize field))))

(defn persist 
  "Persists the specified value for the given field;
  or persists the value for the given field of the specified hash key.
  If the field name of hash field is a clojure keyword, its name will be used"
  ([field value]
   (redis/set db (normalize field) (normalize value)))
  ([hkey field value]
   (redis/hset db (normalize hkey) (normalize field) (normalize value))))

(defn delete 
  "Delete the specified value of the given field;
  or deletes the value for the given field of the specified hash key.
  If the field name of hash field is a clojure keyword, its name will be used"
  ([field]
   (redis/del db [(normalize field)]))
  ([hkey field]
   (redis/hdel db (normalize hkey) (normalize field))))

(defn flush-database
  "Flushes the entire DB (should be used for tests only"
  []
  (redis/flush-all db))

(defn add-to-set
  "Extends the set of a given key by given value 
  (no multiple values are supported)"
  [key member]
  (redis/sadd db (normalize key) (normalize member)))

(defn retrieve-set
  "Retrieves a set"
  [key]
  (set (redis/smembers db (normalize key))))
