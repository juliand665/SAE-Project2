APRON=/home/sae/apron/japron
export CLASSPATH=./bin:./soot-2.5.0.jar:$(APRON)/*
export LD_LIBRARY_PATH=$(APRON)/:/usr/local/lib

SHELL = /bin/bash

TESTSRC = $(wildcard src/Test*.java)
TESTCLS = $(patsubst src/Test%.java, bin/Test%.class, $(TESTSRC))

$(TESTCLS) : $(TESTSRC) src/Robot.java
	mkdir -p bin
	javac -d bin $^

bin/ch/ethz/sae/Verifier.class : src/ch/ethz/sae/*.java
	mkdir -p bin
	javac -d bin src/ch/ethz/sae/*.java

build : bin/ch/ethz/sae/Verifier.class

% : bin/%.class bin/ch/ethz/sae/Verifier.class
	java ch.ethz.sae.Verifier $(basename $(notdir $<))

test : $(patsubst src/Test%.java, test%, $(TESTSRC))

test% : bin/Test%.class bin/ch/ethz/sae/Verifier.class
	@./test.sh $(basename $(notdir $<))

clean :
	rm -rf bin

.PHONY: clean build
