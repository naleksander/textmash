; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.

(ns textmash.editor
	(:use (textmash menu event stream config caret reflect))
	(:import (javax.swing JPanel ScrollPaneConstants JScrollPane BorderFactory JTextPane JMenuBar JMenu JMenuItem JFrame JTextArea AbstractAction UIManager)
		(java.awt Point BorderLayout Color Dimension Font) (java.awt.event KeyEvent) (java.io InputStream OutputStream
		BufferedReader InputStreamReader PrintStream
		PipedInputStream PipedOutputStream)))

(let[ lines (atom nil) ]

(defn draw-lines[graphics y last textPane pstart pend_y fontHeight]
	(if (<= (.y pstart) pend_y)
		(let [pos (+ (.getElementIndex (-> textPane .getDocument .getDefaultRootElement)
			(.viewToModel textPane pstart)) 1)]
			(if (not= last pos)
				(.drawString graphics (format "%4d" pos) 1 y))
			(setf pstart y (+ (.y pstart) fontHeight))
			(draw-lines graphics (+ y fontHeight) pos textPane pstart pend_y fontHeight))))

(defn create-editor[]
	(let[ editorContainer (JPanel.)
		 color (Color. 230 230 230) 
		 font (Font. (get-cfg :font-type) Font/PLAIN (get-cfg :font-size))
		 width (+ 3 (.stringWidth  (.getFontMetrics editorContainer font) "89745"))
		 scrollPane (JScrollPane. (proxy[JTextPane][]
				(paint[graphics] 
					(proxy-super paint graphics)
					(.repaint @lines))))
		 textPane (-> scrollPane .getViewport .getView)]

		 (reset! lines (proxy[JPanel][]
			(paint[graphics] 
				(proxy-super paint graphics)
				(doto graphics
					(.setColor color)
					(.drawLine (- width 1) 0 (- width 1) (.getHeight editorContainer))
					(.setFont font)
					(.setColor Color/GRAY))
					(let [pstart (-> scrollPane .getViewport .getViewPosition)
						start (.viewToModel textPane pstart)
						pend_y (+ (.y pstart) (.getHeight textPane))
						pfont (.getFont textPane)
						pmet (.getFontMetrics graphics pfont)
						fontHeight (.getHeight pmet)
						fontDesc (.getDescent pmet)
						starting_y (- (+ (- (.y (ivkm textPane modelToView start)) 
						(.y pstart)) fontHeight) fontDesc)]
						(draw-lines graphics starting_y -1 textPane pstart
								pend_y fontHeight)))))

		(doto @lines
			(.setBackground (.getBackground textPane))
			(.setPreferredSize  (Dimension. width width)))

		(create-caret textPane)
		(.setFont textPane font)

		(doto scrollPane
			(.setVerticalScrollBarPolicy ScrollPaneConstants/VERTICAL_SCROLLBAR_ALWAYS)
			(.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_AS_NEEDED)
			(.setBorder (BorderFactory/createEmptyBorder)))
			(doto editorContainer (.setLayout  (BorderLayout.))
			(.add scrollPane BorderLayout/CENTER)
			(.add @lines BorderLayout/WEST))
			 editorContainer)))


