(ns trptcolin.trymplicit
  (:require [riddley.walk :as walk]))

(declare add-try)

(defn should-transform? [x]
  (and (seq? x)
       (#{'fn* 'do 'loop* 'let* 'letfn* 'reify*} (first x))))

(defn- wrap-fn-body [[bindings & body]]
  (list bindings (cons 'try body)))

(defn- wrap-bindings [bindings]
  (->> bindings
       (partition-all 2)
       (mapcat
         (fn [[k v]]
           (let [[k v] [k (add-try v)]]
             [k v])))
       vec))

(defn- wrap-fn-decl [clauses]
  (let [[name? args? fn-bodies]
          (if (symbol? (first clauses))
            (if (vector? (second clauses))
              (do
                [(first clauses) (second clauses) (drop 2 clauses)])
              [(first clauses) nil (doall (map wrap-fn-body (rest clauses)))])
            [nil nil (doall (map wrap-fn-body clauses))])]
    (cond->> fn-bodies
      (and name? args?) (#(list (cons 'try %)))
      (not (and name? args?)) (map add-try)
      args? (cons args?)
      name? (cons name?))))

(defn transform [x]
  (condp = (first x)
    'do
    (let [[_ & body] x]
      (cons 'try (add-try body)))

    'loop*
    (let [[verb bindings & body] x]
      `(~verb ~(wrap-bindings bindings) (try ~@(add-try body))))

    'let*
    (let [[verb bindings & body] x]
      `(~verb ~(wrap-bindings bindings) (try ~@(add-try body))))

    'letfn*
    (let [[verb bindings & body] x]
      `(~verb ~(wrap-bindings bindings) (try ~@(add-try body))))

    'fn*
    (let [[verb & fn-decl] x]
      `(fn* ~@(wrap-fn-decl fn-decl)))

    'reify*
    (let [[verb options & fn-decls] x]
      `(~verb ~options ~@(map wrap-fn-decl fn-decls)))

    x))

(defn add-try [expression]
  (walk/walk-exprs should-transform? transform expression))

(defmacro with-implicit-try [& body]
  (cons 'do (map add-try body)))

