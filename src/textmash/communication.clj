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
			InetSocketAddress InetAddress Socket ServerSocket] 
		[java.io BufferedReader ByteArrayOutputStream 
			ByteArrayInputStream InputStreamReader PrintStream]))

(def *remote-peer*)

(defmacro bound-thread[ & body ]
	`(let [ fnc# (bound-fn [] ~@body)]
		(doto (Thread. (reify Runnable (run[this] (fnc#) ))) 
			(.setPriority Thread/MAX_PRIORITY) (.start))))

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


(comment (def s (send-datagram "127.0.0.1:12345" 2000 (fn[] "ala")))

(def r (receive-datagram 12345 (fn[ a m ] (println a "sent" m))))

(comment (.close s)
(.close r)))
