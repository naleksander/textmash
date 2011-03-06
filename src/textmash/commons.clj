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
		(doto (Thread. (reify Runnable (run[this] (fnc#) ))) (.start) identity)))

(defmacro bound-future[ & body ]
	`(future-call (bound-fn [] ~@body)))

(defmacro bound-daemon[ & body ]
	`(let [ fnc# (bound-fn [] ~@body)]
		(doto (Thread. (reify Runnable (run[this] (fnc#) ))) 		
			(.setPriority Thread/MAX_PRIORITY)
			(.setDaemon true)
			(.start) identity)))

(defmacro schedule[frequency & content]
	`(bound-daemon 
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

(defmacro op-in-using
	[ data keys ele op & args ]
		`(if-let [s# (get-in ~data ~keys)] 
			(update-in ~data ~keys ~op ~@args)
			(-> ~data (assoc-in ~keys ~ele)
				(update-in ~keys ~op ~@args))))



(defn uniassoc [col key val]
	(if (map? col)
		(assoc col key val)
		(if (and (number? key) (neg? key))
			(assoc col (+ (count col) key) val)
			(assoc col key val))))

(defn uniget[ v idx ]
	(if (and (number? idx) (neg? idx))
		(get v (+ (count v) idx))
		(get v idx)))

(defn uniget-in[m ks]
     (reduce uniget m ks))

(defn uniupdate-in[ m [k & ks] f & args ]
	(if ks
		(uniassoc m k (apply uniupdate-in (uniget m k) ks f args)) 
		(uniassoc m k (apply f (uniget m k) args))))

(defn uniassoc-in
  [m [k & ks] v]
  (if ks
    (uniassoc m k (uniassoc-in (get m k) ks v))
    (uniassoc m k v)))

(defmacro uniupdate-in-using
	[ data keys ele op & args ]
		`(if-let [s# (uniget-in ~data ~keys)] 
			(uniupdate-in ~data ~keys ~op ~@args)
			(-> ~data (uniassoc-in ~keys ~ele)
				(uniupdate-in ~keys ~op ~@args))))


