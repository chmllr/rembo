(ns Rembo.test.json
  (:use [clojure.test]
        [Rembo.json]))

(deftest jsonification
         (testing "json wrap and unwrap"
                  (defn test-function [field {:keys [key1 key2]}] 
                    {:field field :key1 key1 :key2 key2})
                  (def json-args
                    "{\"field\":\"value\",\"key1\":\"value1\",\"key2\":\"value2\"}")
                  (is (= json-args (jsonify test-function json-args)) 
                      "the input json will be destructured and transformed back to json correctly")))
