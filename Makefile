JAVAC     = javac
JAVA      = java
JFLAGS    = --release 11 -Xlint:-deprecation
SRCPATH   = VideoGame:v2:.
MAINCLASS = MissileCommandStandalone

.PHONY: compile run clean

compile:
	$(JAVAC) $(JFLAGS) -sourcepath $(SRCPATH) \
	    VideoGame/*.java v2/*.java MissileCommandStandalone.java

run: compile
	$(JAVA) -cp . $(MAINCLASS)

clean:
	rm -f VideoGame/*.class v2/*.class *.class
