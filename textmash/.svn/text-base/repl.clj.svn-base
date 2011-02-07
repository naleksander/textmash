; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.

(ns textmash.repl
	(:use (textmash stream config))
	(:import (javax.swing JMenuBar JMenu JMenuItem AbstractAction)
	(java.awt Dimension) (java.io File InputStream OutputStream
		BufferedReader InputStreamReader PrintStream
		PipedInputStream PipedOutputStream)))

(daemon
	(let [ out (PrintStream. (print-process (get-cfg :encoding) 
			(get-cfg :working-dir) (get-cfg :repl)))]
;			(stream-transfer (System/in) out)
			(doseq [ x (range 5)]
				(.println out (str "(println \"ąśćŻł\" " x " 20)"))
				(.flush out)  (Thread/sleep 2000) ) 
			(.close out)))

(hang-up)


