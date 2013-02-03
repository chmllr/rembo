(ns Rembo.json
  (:require [cheshire.core :refer :all]))

(defn traverse
  "Helper function for traversing argument lists of functions"
  ([f f2 coll-prepro args] (traverse f f f2 coll-prepro args))
  ([f f-in-coll f2 coll-prepro args]
   (map #(if (coll? %) 
           (f2 (traverse f-in-coll f2 coll-prepro (coll-prepro %)))
           (f %)) args)))

(defmacro jsonify
  "Get a function name and JSON args; returns function result as JSON"
  [function-name json-args]
  `(let [argslists# (:arglists (meta (var ~function-name)))
         assertion# (assert (= 1 (count argslists#)))
         args# (traverse str #(vec %) :keys (first argslists#))
         map-args# (parse-string ~json-args)
         func-args# (traverse map-args# 
                              #(hash-map (keyword %) (map-args# %)) 
                              #(apply merge %) identity args#)]
     (generate-string
       (apply ~function-name func-args#))))

