; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.

(ns textmash.menu
		(:import (javax.swing JMenuBar JMenu JMenuItem AbstractAction Action KeyStroke)
		(java.awt Dimension Toolkit)
		(java.awt.event KeyEvent)))


(defn create-action[ meta logic ]
	(let [ act (proxy[AbstractAction] [ (:name meta) ] 
		(actionPerformed[args] (logic args)))]
			(if-let [ ak (:key meta)]
				(.putValue act Action/ACCELERATOR_KEY (KeyStroke/getKeyStroke
					ak  (.getMenuShortcutKeyMask (Toolkit/getDefaultToolkit)))))
			  act))
 

(defn create-menu-item[ id meta actions children ]
	(if-let[ action-logic (id actions) ]
		(if-let [ action (create-action meta action-logic) ]
			(if (seq children) (JMenu. action) (JMenuItem. action))) 
				(if (seq children) (JMenu. (:name meta)) (JMenuItem. (:name meta)))))


(defn menu-bar[ parent definition actions ]
	(doseq [[id meta] definition]
		(let [children (:children meta) 
				menu (create-menu-item id meta actions children)]
					(.add parent (if children
						(menu-bar menu children actions) menu)))) parent)

