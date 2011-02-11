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
	(:use (textmash menu event stream config editor))
	(:import (javax.swing JFrame UIManager)
		(java.awt Dimension) 
		(java.awt.event KeyEvent)))

(UIManager/setLookAndFeel (UIManager/getSystemLookAndFeelClassName))

(defn window[ & { :keys [ title menu content ] } ]
	(let [ f (JFrame. title )]
		(doto f (-> (.getContentPane) 
			(.add content))
			(.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
			(.setJMenuBar menu )
			(.setSize (Dimension. 640 480))
			(.setLocationRelativeTo nil)
			(.setVisible true))))
	
(def menu-definition {
	:file { :name "File" :children {
		 :new  { :name "New" :key KeyEvent/VK_N }
		:open  { :name	"Open..." :key KeyEvent/VK_O } } }
	:view { :name "View" :children {
		:wrapLines { :name "Wrap Lines"  :key KeyEvent/VK_L :checkbox true }

		} }
	:clojure { :name "Clojure" :children {  
	 	:newRepl  { :name "Start New REPL" :key KeyEvent/VK_T }
		:configureRepl  { :name	"Configure" }
		} } 
})

(def actions-definition {
	:wrapLines (fn[args] (fire-event :wrapLines (.isSelected (.getSource args))))
	:new (fn[args] (println "Called new"))
	:open (fn[args] (println "Called open"))
	:newRepl (fn[args] (process (get-cfg :working-dir) (get-cfg :terminal-launcher { :title "Test" })) )
	:configureRepl (fn[args] (println "Called configure REPL"))
	})


(window :title "TextMash" 
			:menu (create-menu menu-definition actions-definition) 
			:content (create-editor) )
	
