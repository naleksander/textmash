; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.

(ns textmash.communication
	(:use (textmash commons))
	(:import [java.net DatagramPacket DatagramSocket 
			InetSocketAddress InetAddress InetSocketAddress Socket ServerSocket] 
		[java.io BufferedReader ByteArrayOutputStream 
			ByteArrayInputStream InputStreamReader PrintStream]))

(def *remote-peer*)
(def *reported-existance* (ref {}))
(def *incoming-listener* (atom nil))

(defn parse-address[ addr ]
	(let [ [x y] (.split addr ":") ]
		[x (Integer/parseInt y)]))

(defn send-periodic-datagram[addr freqency fnc]
	(let [ [host port ] (parse-address addr) ]
	(let [ms (DatagramSocket.) adr (InetAddress/getByName host) 
			baos (ByteArrayOutputStream. 512)]
		(bound-future
		(while (not (.isClosed ms))
			(try
			(.reset baos)
			(let [out (PrintStream. baos)]
				(.println out (prn-str (fnc)))
				(.flush out)
				(let [ba (.toByteArray baos) 
					dp (DatagramPacket. ba 0 (count ba) adr port)]
				  (locking ms (if-not (.isClosed ms)	(.send ms dp) )  )))
						(catch Exception e))(Thread/sleep freqency)))  ms)))


(defn receive-periodic-datagram[port fnc]
	(let [ms (DatagramSocket. port) ba (byte-array 1024)]
		(.setSoTimeout ms 2500)
		(bound-future
			(while (not (.isClosed ms))
				(try
				(let [dp (DatagramPacket. ba (count ba))]
			(locking ms (if-not (.isClosed ms) (.receive ms dp)))
			(fnc (str (.getHostAddress (.getAddress dp)) ":" (.getPort dp) )
				(read-string (.readLine 
					(BufferedReader. (InputStreamReader.
						 (ByteArrayInputStream. (.getData dp) 0 (.getLength dp))))))  )) 
						(catch Exception e)))) ms ))


(defn call-remote* [addr type cmd data]
	(let [ [host port ] (parse-address addr) sck (Socket. ) ]
			(.connect sck  (InetSocketAddress. 
				(InetAddress/getByName host) port) 2500)
		(let [out (PrintStream. (.getOutputStream sck))
			in (BufferedReader. (InputStreamReader. (.getInputStream sck))) ]
			(.println out type)
			(.println out cmd)
			(.println out data)
			(.flush out)
			(let [ result (.readLine in) 
					content (read-string (.readLine in))]
				(if (= result "ok")
					content
					(throw (Exception. content)))))))


(defmacro call-remote [ addr cmd & data ]
	`(call-remote* ~addr "synch" (str '~cmd) ~(prn-str data)))

(defmacro asynch-call-remote [ addr cmd & data ]
	`(call-remote* ~addr "asynch" (str '~cmd) ~(prn-str data)))

(defmacro eval-call-remote [ addr data]
	`(call-remote* ~addr "eval" "eval" ~(prn-str data)))

(defn- synch-call[execfn fc in out]
	(let [args (read-string (.readLine in))]
		(try
			(let[ result (execfn fc args)]
				(.println out "ok")
				(.println out (prn-str result)))
			(catch Exception e 
				(do
					(.println out "failed")
					(.println out (prn-str (with-out-str (.printStackTrace e))))))) 
				(.flush out)))

(defn- asynch-call[fc in out]
	(let [args (read-string (.readLine in))]
		(.println out "ok")
		(.println out (prn-str nil))
		(.flush out)
		(bound-future
			(apply fc args))))

(defn bind-remote[ port & commands-args ]
	(let[ commands (cons eval commands-args) ]
		(let [cmds (zipmap (map #(str (:name (meta %1))) commands) commands) 
		  cmd-type { "synch" (partial synch-call (fn[ fc args] (apply fc args)))
					 "asynch" asynch-call 
					 "eval" (partial synch-call (fn[ fc args] (fc args))) } ]
		(let [s1 (ServerSocket. port)]
			(.setSoTimeout s1 2500)
			(bound-future (while (not (.isClosed s1))
				(if-let [c1 (try (.accept s1) (catch Exception e nil))]
					(bound-future (binding [*remote-peer* 
						(str (.getHostName (.getInetAddress c1)) ":" (.getPort c1) )]
						(try
							(let [in (BufferedReader. (InputStreamReader. (.getInputStream c1)))
								  out (PrintStream. (.getOutputStream c1))
								  cmdtype (.readLine in) cmdname (.readLine in)]
										((get cmd-type cmdtype) (get cmds cmdname) in out))
											(catch Exception e2 (.printStackTrace e2))
												(finally (.close c1)))))))) s1))))

(defn active-units-listener[timeout incoming-fnc outcoming-fnc]
	(reset! *incoming-listener* incoming-fnc)
	(schedule (/ timeout 2)
		(let[ outcoming (dosync 
			(let [ outcoming (map first (filter (fn[[k v]] (< v (now (- timeout)))) @*reported-existance*))]
				(alter *reported-existance* #(apply dissoc %1 %2) outcoming) outcoming)) ]
				(if (not-empty outcoming) (outcoming-fnc outcoming)))))


(defn report-active-unit[ address ]
	(if-not (dosync 
		(let[ incoming (contains? @*reported-existance* address) ]
			(alter *reported-existance* assoc address (now)) incoming)) 
				(if-let [ incoming-fnc @*incoming-listener* ] (incoming-fnc address))))

(defn get-active-units[]
	(map first @*reported-existance*))


;(existance-listener 5000 (fn[ b] (println "Incoming" b)) (fn[ a] (println "Outcoming" a)))

;(report-existance "127.0.0.1:12345")
;(report-existance "127.0.0.1:12346")
;
;(defn test-me[ a ]
;	;(throw (RuntimeException. "owowow"))
;	(println "processing" a))
;
;(def lc (bind-remote 12345 println str test-me))
;(.close lc)
;;;
;;(asynch-call-remote "127.0.0.1:12345" println "hello" "world" 12)
;;
;(call-remote "127.0.0.1:12345" test-me "none")
;;
;
;(eval (read-string (prn-str '(println2 2))))
;
;;(.printStackTrace *e)
;
;(println "got" (eval-call-remote  "127.0.0.1:12345" (do (println (+ 2 3)) 
;	(throw (Exception. "alalala")) (println  "wpwppwpw") 2) ))
;
;
;(def s (send-datagram "127.0.0.1:12345" 2000 (fn[] "ala")))
;
;(def r (receive-datagram 12345 (fn[ a m ] (println a "sent" m))))
;
;(.close s)
;(.close r)
