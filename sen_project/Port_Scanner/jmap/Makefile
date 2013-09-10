# makefile for JMap
# GNU GPL

all:	jmap
	jar -cvmf manifest.txt jmap.jar *.class ports.defs

jmap: 	
	javac *.java

javadoc:
	javadoc -d javadoc/ -version -author -windowtitle GNU-JMAP_Java_Port_Scanner *.java

clean:
	-rm *.class
	-rm jmap.jar
	-rm -rf javadoc/

