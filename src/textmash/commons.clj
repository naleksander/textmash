; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.

(ns textmash.commons)

(defn now
  ([] (System/currentTimeMillis))
  ([n] (+ (now) n)))

(defmacro bound-thread[ & body ]
	`(let [ fnc# (bound-fn [] ~@body)]
		(doto (Thread. (reify Runnable (run[this] (fnc#) ))) (.start))))

(defmacro bound-future[ & body ]
	`(future-call (bound-fn [] ~@body)))

(defmacro schedule[frequency & content]
	`(bound-future 
			(loop[]
				(do ~@content)
				(Thread/sleep ~frequency)
				(recur))))

(defn dissoc-in 
	[ data keys & keys-to-remove ]
		(if (seq keys)
			(reduce #(assoc-in %1 keys 
				(dissoc (get-in %1 keys) %2)) data keys-to-remove)  
			(apply dissoc data keys-to-remove)))

(defmacro update-in-using
	[ data keys ele op & args ]
		`(if-let [s# (get-in ~data ~keys)] 
			(update-in ~data ~keys ~op ~@args)
			(-> ~data (assoc-in ~keys ~ele)
				(update-in ~keys ~op ~@args))))


(defn contains-in?[ m keys ]
	(not (nil? (get-in m keys))))


(defn update-in-get
	[ data keys & op-and-args ]
		(let [new-data (apply update-in data keys op-and-args)
				 new-value (get-in new-data keys) ]
				[new-data new-value]))




