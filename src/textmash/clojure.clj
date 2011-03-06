; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.

(ns textmash.clojure
	(:use (textmash commons)))

; This package is made just for fun


(def y2 "     (def x \"Hello \\\"Hulla\\\" world\")
(println (+ 3 4 ) x)
(+ 2 3)  ")

(def y " (println (+ 2 (* 4 5)) (str \"nice\" \"good job\") ) ")

(def x (map second (re-seq #"\s*(\w+|[\(\)]|[\+\-\*\/]|\"([^\"]|\")*?\")\s*" y)))

(doseq [ z x ]
	(println z))

(defn evil[ s x ]
	(let[ [ y & rx ] x ]
		(if (nil? y)
			(first s)
			(if (= y ")")
				[ s rx ]
				(if (= y "(")
					(let[ a (evil [] rx) [ ff rxr ] a ]
						(evil (conj s (apply (first ff) (rest ff))) rxr))
						(evil (conj s (load-string y)) rx))))))

(evil [] x)

