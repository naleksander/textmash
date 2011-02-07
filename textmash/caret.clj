; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.

(ns textmash.caret
	(:use (textmash reflect))
	(:import (javax.swing.text BadLocationException DefaultCaret JTextComponent)
		(java.awt Graphics Rectangle)))


(defn create-caret[]
	(proxy [DefaultCaret][]
		(damage[rectangle] 
			(if (not (nil? rectangle))
				(do (setf this x (.x rectangle))
				(setf this y (.y rectangle))
				(setf this height (.height rectangle))
				(if (<= (getf this width) 0)
					(setf this width  (.getWidth (ivkm this getComponent))))
				(ivkm this repaint))
			))
		(paint[graphics] 
			(if-let [comp (ivkm this getComponent)]
				(let[ dot (.getDot this)]
					(if-let[ rectangle (.modelToView comp dot)]
						(let[ dotChar (.charAt (.getText comp dot 1) 0)]
			(if (or (not= (.x this) (.x rectangle)) (not= (.y this) (.y rectangle)))
				(do (ivkm this repaint)
				(setf this x (.x rectangle))
				(setf this y (.y rectangle))
				(setf this height (.height rectangle))))
			(.setColor graphics (.getCaretColor comp))
			(setf this width 2)
			(if (.isVisible this)
				(.fillRect graphics (.x rectangle) (.y rectangle) (getf this width) (.height rectangle)))
			)))))))

