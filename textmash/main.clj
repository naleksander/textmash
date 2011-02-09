; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.

(System/setProperty "apple.laf.useScreenMenuBar" "true")
(System/setProperty "com.apple.mrj.application.apple.menu.about.name" "TextMash")

(ns textmash.main
	(:use (textmash menu event stream config caret editor))
	(:import (javax.swing JMenuBar JMenu JMenuItem JFrame JTextArea AbstractAction UIManager)
		(java.awt FontMetrics Dimension Font Color Point) 
		(java.awt.event KeyEvent) (java.io InputStream OutputStream
		BufferedReader InputStreamReader PrintStream
		PipedInputStream PipedOutputStream)))

(UIManager/setLookAndFeel (UIManager/getSystemLookAndFeelClassName))

(defn window[ data ]
	(let [ f (JFrame. (:title data) )]
		(doto f (-> (.getContentPane) 
			(.add (:content data)))
			(.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
			(.setJMenuBar (:menu data) )
			(.setSize (Dimension. 640 480))
			(.setLocationRelativeTo nil)
			(.setVisible true))))
	
(def menu-definition {
	:file { :name "File" :children {
		 :new  { :name "New" :key KeyEvent/VK_N }
		:open  { :name	"Open..." :key KeyEvent/VK_O } } }
	:clojure { :name "Clojure" :children {  
	 	:newRepl  { :name "Start New REPL" :key KeyEvent/VK_T }
		:configureRepl  { :name	"Configure" }
		} } 
})

(def actions-definition {
	:new (fn[args] (println "Called new"))
	:open (fn[args] (println "Called open"))
	:newRepl (fn[args] (process (get-cfg :working-dir) (get-cfg :terminal-launcher { :title "Test" })) )
	:configureRepl (fn[args] (println "Called configure REPL"))
	})


(window { :title "TextMash" 
			:menu (menu-bar (JMenuBar.) menu-definition actions-definition) 
			:content (create-editor) })
	
