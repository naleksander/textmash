; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.


(ns textmash.event
	(:import (java.util UUID)))

(def *listeners* (atom {}))

(defn uuid[]
	(.toString (UUID/randomUUID)))

(defmacro update-in-using
	[ data keys ele op & args ]
		`(if-let [s# (get-in ~data ~keys)] 
			(update-in ~data ~keys ~op ~@args)
			(-> ~data (assoc-in ~keys ~ele)
				(update-in ~keys ~op ~@args))))

(defn dissoc-in 
	[ data keys & keys-to-remove ]
		(if (seq keys)
			(reduce #(assoc-in %1 keys 
				(dissoc (get-in %1 keys) %2)) data keys-to-remove)  
			(apply dissoc data keys-to-remove)))

(defn register-event[event-name listener-fn]
	(let [gen-uuid (uuid)]
		(swap! *listeners* 
			(fn[lns] 
				(-> lns (assoc-in [:event event-name gen-uuid] listener-fn)
					(assoc-in [:listeners gen-uuid] event-name )))) gen-uuid) )

(defn fire-event[event-name & args]
	(letfn [( events [ fns ]
		(if (seq fns)
			(if-let[ r (apply (first fns)  args) ]
				 (cons r (lazy-seq (events (rest fns)))) (cons false []))))] 
					(every? true? (events (map second (get-in @*listeners* [:event event-name]))))))

(defn unregister-event[uuid]
	(swap! *listeners* (fn[lns] 
		(let[ event-name (get-in lns [:listeners uuid]) ]
			(-> lns (dissoc-in [:listeners] uuid)
				(dissoc-in [:event event-name ] uuid))))))

(comment (register-event "tom" (fn[ a ] (println a) false))
(register-event "tom" (fn[ a ] (println "heheh" a) true))
(register-event "tom" (fn[ a ] (println "lo siento" a) true))
(fire-event "tom" 123))

