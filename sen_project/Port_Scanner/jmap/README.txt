(This file is in UNIX format, any good text editor will read it ok, not notepad)


JMap - Java Network Port Scanner
Tom Salmon	tom@slashtom.org





Compiling Source code:

	Under windows:
		javac *.java
		jar -cvmf manifest.txt jmap.jar *.class ports.defs
	
	
	Under UNIX (or similar ie. GNU/Linux, Mac OS X):
		make


When compiled, the program is stored in an executable 'jar' file:
'jmap.jar'

To run, either 'double-click' on 'jmap.jar' 
or from the command line: "java -jar jmap.jar"



Generating Javadoc code documentation:

	Under Windows:
		javadoc -d javadoc/ -version -author -windowtitle GNU-JMAP_Java_Port_Scanner *.java

	Under UNIX (or similar ie. GNU/Linux, Mac OS X):
		make javadoc

HTML documentation will be placed in a subdirectory of the jmap source, called 
'javadoc'.



			- Tom Salmon (11/01/2003)
