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
	(:use (textmash menu event stream config reflect))
	(:import (javax.swing.text  DefaultCaret  DefaultStyledDocument   AbstractDocument BoxView ComponentView
		  IconView LabelView ParagraphView StyleConstants StyledEditorKit View ViewFactory))
	(:import (javax.swing JPanel ScrollPaneConstants JScrollPane BorderFactory JTextPane)
		(java.awt  Rectangle  BorderLayout Color Dimension Font)))

(defn draw-lines[graphics y last textPane pstart pend_y fontHeight]
	(if (<= (.y pstart) pend_y)
		(let [pos (+ (.getElementIndex (-> textPane .getDocument .getDefaultRootElement)
			(.viewToModel textPane pstart)) 1)]
			(if (not= last pos)
				(.drawString graphics (format "%4d" pos) 1 y))
			(setf pstart y (+ (.y pstart) fontHeight))
			(draw-lines graphics (+ y fontHeight) pos textPane pstart pend_y fontHeight))))

(defn wrap-editor-kit[textPane]
	(proxy[StyledEditorKit] []
		(createDefaultDocument[] (DefaultStyledDocument.))))

(defn nowrap-editor-kit[textPane]
	(proxy[StyledEditorKit][]
		(getViewFactory[] (proxy[ViewFactory][]
			(create[ elem ]
				(if-let[ kind (.getName elem)]
				(condp (fn[a b] (.equals a b)) kind
					AbstractDocument/ContentElementName (LabelView. elem)
					AbstractDocument/ParagraphElementName (ParagraphView. elem)
					AbstractDocument/SectionElementName (proxy[BoxView][elem View/Y_AXIS]
							(layout [width height] (proxy-super layout 32768 height))
							(getMinimumSpan[axis] (proxy-super getPreferredSpan axis)))
					StyleConstants/ComponentElementName (ComponentView. elem)
					StyleConstants/IconElementName (IconView. elem))
				(LabelView. elem))
			)))
		(createDefaultDocument[] (DefaultStyledDocument.))))


(defn create-caret[textPane]
	(let [caret (proxy [DefaultCaret][]
		(damage[^Rectangle rectangle] 
			(if (not (nil? rectangle))
				(do 
				(ivkm this setLocation  (.x rectangle)  (.y rectangle))
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
				(ivkm this setLocation  (.x rectangle)  (.y rectangle))
				(setf this height (.height rectangle))))
			(.setColor graphics (.getCaretColor comp))
			(setf this width 2)
			(if (.isVisible this)
				(.fillRect graphics (.x rectangle) (.y rectangle) (getf this width) (.height rectangle)))
			))))))]

		(.setBlinkRate caret (.getBlinkRate (.getCaret textPane)))
		(.setCaret textPane caret)
		caret))

(let[ lines (atom nil) pane (atom nil) ]

(defn wrap-lines[wrap]
	(let[ textPane @pane wrapPolicy (if wrap (wrap-editor-kit textPane) (nowrap-editor-kit textPane))]
			(let[ doc (.getDocument textPane)]
				(.setEditorKit textPane wrapPolicy)
				(.setDocument textPane doc))))

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

		 (reset! pane textPane)
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

		(doto textPane (create-caret) (.setFont font))

		;TODO: Subject to change
		(wrap-lines false)

		(doto scrollPane
			(.setVerticalScrollBarPolicy ScrollPaneConstants/VERTICAL_SCROLLBAR_ALWAYS)
			(.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_AS_NEEDED)
			(.setBorder (BorderFactory/createEmptyBorder)))
		(doto editorContainer (.setLayout  (BorderLayout.))
			(.add scrollPane BorderLayout/CENTER)
			(.add @lines BorderLayout/WEST))
			 editorContainer))

;TODO: Subject to change
(register-event :wrapLines (fn[ wrap ] (wrap-lines wrap) true))

)


