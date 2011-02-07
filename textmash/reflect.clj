; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.

(ns textmash.reflect)

(def find-member-st (atom {}))

(defmacro find-member [access-type check-fn method-name obj]
	`(if-let [mb# (get-in @find-member-st [~(name access-type) ~(class obj) ~(name method-name)])]
		mb#
		(letfn [ (find-rec#[ clazz# ]
		(if (not (nil? clazz#))
			(let [ms# (filter #(and (= (.getName %1) ~(name method-name)) (~check-fn %1) ) 
				(. clazz# ~access-type ))]
					(if-let [fms# (first ms#) ] (do (swap! find-member-st (fn[a#] (assoc-in a# [ ~(name access-type) 
						~(class obj) ~(name method-name) ] fms#  ))) (.setAccessible fms# true) fms# )
						(find-rec# (.getSuperclass clazz#))))
						  ))] (find-rec# (class ~obj)) )) )

; (find-member getDeclaredFields  (constantly true) width (Rectangle.))

(defmacro ivkm [ obj method & args ]
	`(if-let [ m# (find-member getDeclaredMethods
				#(= ~(count args) (count (.getParameterTypes %1)))
			 ~method ~obj )]
		(.invoke  m# ~obj (into-array Object (vector ~@args)) )
			(throw (Exception. (str "Failed to invoke " ~(name method))))))

(defmacro setf[ obj method arg ]
	`(if-let [ m# (find-member getDeclaredFields (constantly true) ~method ~obj )]
		(.set  m# ~obj ~arg )
			(throw (Exception. (str "Failed to set field " ~(name method))))))

(defmacro getf[ obj method ]
	`(if-let [ m# (find-member getDeclaredFields (constantly true) ~method ~obj )]
		(.get  m# ~obj )
			(throw (Exception. (str "Failed to get field " ~(name method))))))




