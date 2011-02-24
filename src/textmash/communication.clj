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
	(:import [java.net DatagramPacket DatagramSocket 
			InetSocketAddress InetAddress InetSocketAddress Socket ServerSocket] 
		[java.io BufferedReader ByteArrayOutputStream 
			ByteArrayInputStream InputStreamReader PrintStream]))

(def *remote-peer*)

(defmacro bound-thread[ & body ]
	`(let [ fnc# (bound-fn [] ~@body)]
		(doto (Thread. (reify Runnable (run[this] (fnc#) ))) (.start))))

(defmacro bound-future[ & body ]
	`(future-call (bound-fn [] ~@body)))

(defn parse-address[ addr ]
	(let [ [x y] (.split addr ":") ]
		[x (Integer/parseInt y)]))

(defn send-periodic-datagram[addr freqency fnc]
	(let [ [host port ] (parse-address addr) ]
	(let [ms (DatagramSocket.) adr (InetAddress/getByName host) 
			baos (ByteArrayOutputStream. 512)]
		(bound-thread
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
		(bound-thread
			(while (not (.isClosed ms))
				(try
				(let [dp (DatagramPacket. ba (count ba))]
			(locking ms (if-not (.isClosed ms) (.receive ms dp)))
			(fnc (str (.getHostAddress (.getAddress dp)) ":" (.getPort dp) )
				(read-string (.readLine 
					(BufferedReader. (InputStreamReader.
						 (ByteArrayInputStream. (.getData dp) 0 (.getLength dp))))))  )) 
						(catch Exception e)))) ms ))


(defn- call-remote* [addr cmd & data]
	(let [ [host port ] (parse-address addr) sck (Socket. ) ]
			(.connect sck  (InetSocketAddress. 
				(InetAddress/getByName host) port) 2500)
		(let [out (PrintStream. (.getOutputStream sck))
			in (BufferedReader. (InputStreamReader. (.getInputStream sck))) ]
			(.println out cmd)
			(.println out (prn-str data))
			(.flush out)
			(read-string (.readLine in)))))

(defmacro call-remote [ addr cmd & data ]
	`(call-remote* ~addr (str '~cmd) ~@data))


(defn bind-remote[ port & commands ]
	(let [cmds (zipmap (map #(str (:name (meta %1))) commands) (map (fn[fc]
		(fn [in out]
			(.println out (prn-str (apply fc 
				(read-string (.readLine in))))) (.flush out))) commands)) ]
		(let [s1 (ServerSocket. port)]
			(.setSoTimeout s1 2500)
			(bound-future (while (not (.isClosed s1))
				(if-let [c1 (try (.accept s1) (catch Exception e nil))]
					(bound-future (binding [*remote-peer* 
						(str (.getHostName (.getInetAddress c1)) ":" (.getPort c1) )]
						(try
						(let [in (BufferedReader. (InputStreamReader. (.getInputStream c1)))
							  out (PrintStream. (.getOutputStream c1))
							  cmdname (.readLine in)]
							(if-let [ fc (get cmds cmdname)]
								(fc in out)
								(throw (Exception. (str "unknown command: " cmdname)))))
									(catch Exception e2 (.printStackTrace e2))
										(finally (.close c1)))))))) s1)))



;(def lc (bind-remote 12345 println str))
;(.close lc)
;
;(call-remote "127.0.0.1:12345" println "hello" "world" 12)
;
;(def s (send-datagram "127.0.0.1:12345" 2000 (fn[] "ala")))
;
;(def r (receive-datagram 12345 (fn[ a m ] (println a "sent" m))))
;
;(.close s)
;(.close r)
