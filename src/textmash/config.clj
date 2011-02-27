; TextMash - simple IDE for Clojure
; 
; Copyright (C) 2011 Aleksander Naszko
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
;
; You must not remove this notice, or any other, from this software.

(ns textmash.config)

(def *config* {
	:win {
		:working-dir "D:/textmash/TextMash2"
		:clojure-runtime "C:/Clojure/clojure.jar;C:/Clojure/clojure-contrib.jar"
		:terminal-launcher [ "cmd" "/c" "start" "cmd" "/c" "java" "-cp" 
				"${sys:java.class.path};${cfg:working-dir};${cfg:clojure-runtime}"
				 "clojure.main" "textmash/repl.clj" "0" "${arg:title}"]
		:repl [ "java" "-cp" "${cfg:working-dir};${cfg:clojure-runtime}" "clojure.main" ]
		:encoding "cp852"
		:font-type "Courier New"
		:font-size 13
	}
	:mac {
		:working-dir "/Users/aleksander/ClojureProjects/textmash/src"
		:clojure-runtime "/Users/aleksander/.clojure/clojure-1.2.0.jar:/Users/aleksander/.clojure/clojure-contrib-1.2.0.jar"
		:terminal-launcher [ "osascript" 
		    "-e" "tell app \"Terminal\" to do script with command \"cd ${cfg:working-dir};/usr/bin/clear; java -cp ${sys:java.class.path}:${cfg:working-dir}:${cfg:clojure-runtime} clojure.main textmash/repl.clj 0 ${arg:title}\"" 
		    "-e" "tell app \"Terminal\" to set custom title of front window to \"${arg:title}\"" 
		    "-e" "tell app \"Terminal\" to activate" ]
		:repl [ "java" "-cp" "${cfg:working-dir}:${cfg:clojure-runtime}" "clojure.main" ]
		:encoding "UTF-8"
		:font-type "Monaco"
		:font-size 12
	}
})

(defn get-or[ fns val dval ]
	(or (fns val) dval))

(defn get-sys
	([ val ] (get-sys val nil))
	([ val dval ] (get-or #(System/getProperty %1) val dval)))

(defn get-env
	([ val ] (get-env val nil))
	([ val dval ] (get-or #(System/getenv %1) val dval)))

(defn get-os[]
	(condp #(.contains %2 %1) (.toLowerCase (get-sys "os.name"))
		"mac" :mac "win" :win "linux" :linux))

(let [ er { \$ "\\$" \{ "\\{" \} "\\}" }]
	(defn escape-regex[ val ]
		(apply str (map (fn[a] (get er a a)) (seq val)))))

(defn process-config[ line mp ]
	(if (sequential? line) (into [] (map #(process-config %1 mp) line))
		(if (not= String (class line)) line
		(reduce (fn[ processed-line [org spa val] ]
		(.replaceAll processed-line (escape-regex org) ((get mp (keyword spa)) val)))
		 line (map (fn[p] (re-find #"\$\{(.*?):(.*?)\}" p)) (re-seq #"\$\{[^\}]+\}" line))))))

(let [os (get-os)]
	(defn get-cfg
		([ fk ]
			(get-cfg fk {} ))
		([ fk def-vals ]
			(process-config (get-in *config* [ os fk ]) {
				:cfg (fn[ val ] (get-cfg (keyword val) def-vals))
				:arg (fn[ val ] ((keyword val) def-vals))
				:sys (fn[ val ] (get-sys val))
				:env (fn[ val ] (get-env val)) }))))


