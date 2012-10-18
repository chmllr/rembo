(ns Rembo.test.core
  (:use [Rembo.core])
  (:use [clojure.test]))

(testing "API"
         (do (user-create "elrodeo" "asdasd" "I'm nothing" "asd@asd.de")
         ))
