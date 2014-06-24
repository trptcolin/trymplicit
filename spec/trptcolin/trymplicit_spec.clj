(ns trptcolin.trymplicit-spec
  (:require [speclj.core :refer :all]
            [trptcolin.trymplicit :refer :all]))

(describe "with-implicit-try"

    (it "handles do"
      (with-implicit-try
        (should= :caught
                 (do (throw (IllegalArgumentException. "oops"))
                     (catch IllegalArgumentException e
                       :caught)))))

    (it "handles old-style fn*"
      (with-implicit-try
        (should= :caught
                 ((fn* hi []
                       (throw (IllegalArgumentException. "oops"))
                       (catch IllegalArgumentException e :caught))))))

    (it "handles fn macroexpansion"
      (with-implicit-try
        (should= :caught
                 ((fn oh []
                    (throw (IllegalArgumentException. "oops"))
                    (catch IllegalArgumentException e :caught))))))

    (it "handles nested lets"
      (with-implicit-try
        (should= :rescue
                 (let [x (let [y :y-val]
                           (throw (IllegalArgumentException. "oopsie"))
                           (catch Exception e :rescue))]
                   x))))

    (it "handles fn inside a let"
      (with-implicit-try
        (should= "gotcha"
                 (let [i 0]
                   ((fn [] (throw (Exception. "foo"))
                      (catch Exception inner "gotcha")))
                   (catch Exception outer i)))))

    (it "handles letfn"
      (with-implicit-try
        (should= :outer-caught
                 (letfn [(f1 [] (catch Exception e :inner-caught))]
                   (throw (Exception. "oops"))
                   (catch Exception e :outer-caught)))))

    (it "handles reify"
      (with-implicit-try
        (should= "caught"
                 (.toString (reify Object
                              (toString [this]
                                (throw (Exception. "foobar"))
                                (catch Exception e "caught")))))))

    (it "handles loop"
      (with-implicit-try
        (should= 0
                 (loop [i 0]
                   (when (< i 5) (throw (RuntimeException. "oopsie!")))
                   (catch Exception e i)))))


    )
