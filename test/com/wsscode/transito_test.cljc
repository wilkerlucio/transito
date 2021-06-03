(ns com.wsscode.transito-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest is testing]]
    [com.wsscode.transito :as transito]))

(deftest write-str-test
  (is (= (transito/write-str {})
         "[\"^ \"]"))

  (testing "meta round-trip"
    (is (= (transito/write-str ^:mmm {:foo "bar"})
           "[\"~#with-meta\",[[\"^ \",\"~:foo\",\"bar\"],[\"^ \",\"~:mmm\",true]]]")))

  (testing "unknown types"
    (is (str/starts-with?
          (transito/write-str (atom 2))
          "[\"~#unknown\",\"#object[clojure.lang.Atom"))))

(deftest read-str-test
  (is (= (-> (transito/write-str {})
             (transito/read-str))
         {}))

  (testing "meta round-trip"
    (is (= (-> (transito/write-str ^:mmm {:foo "bar"})
               (transito/read-str)
               meta)
           {:mmm true}))))
