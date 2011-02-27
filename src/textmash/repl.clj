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
	(:use (textmash stream communication config))
	(:import (javax.swing JMenuBar JMenu JMenuItem AbstractAction)
	(java.awt Dimension) (java.io File InputStream OutputStream
		BufferedReader InputStreamReader PrintStream
		PipedInputStream PipedOutputStream)))

(def repl-out (PrintStream. (print-process (get-cfg :encoding) (get-cfg :working-dir) (get-cfg :repl))))

(defn execute-statement[ statement ]
	(.println repl-out statement)
	(.flush repl-out))

(def repl-remote (bind-remote 0 execute-statement))

(send-periodic-datagram "127.0.0.1:3246" 2500 (fn[]
	(.getLocalPort repl-remote)))

(hang-up)


