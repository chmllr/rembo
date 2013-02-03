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

(defn traverse
  "Helper function for traversing arg lists of functions"
  ([f f2 coll-prepro args] (traverse f f f2 coll-prepro args))
  ([f f-in-coll f2 coll-prepro args]
   (map #(if (coll? %) 
           (f2 (traverse f-in-coll f2 coll-prepro (coll-prepro %)))
           (f %)) args)))

(defmacro get-json [function-name json-args]
  `(let [argslists# (:arglists (meta (var ~function-name)))
         assertion# (assert (= 1 (count argslists#)))
         args# (traverse str #(vec %) :keys (first argslists#))
         map-args# (parse-string ~json-args)
         func-args# (traverse map-args# 
                              #(hash-map (keyword %) (map-args# %)) 
                              #(apply merge %) identity args#)]
     (generate-string
       (apply ~function-name func-args#))))

