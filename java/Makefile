
.PHONY: all clean test compile

MAL_MAIN := ../coreLang/main.mal

CRAP := *.class

all: compile

compile: Generate.class

clean:
	rm -f $(wildcard $(CRAP))

test: m1.dot.png
	feh m1.dot.png &

Generate.class: $(wildcard *.java)
	javac -cp json-simple-1.1.1.jar *.java

run: Generate.class
	java -cp json-simple-1.1.1.jar:. Generate ${MAL_MAIN}

m1.dot: Generate.class
	java -cp json-simple-1.1.1.jar:. Generate ${MAL_MAIN}


%.dot.png: %.dot
	dot -Tpng -o '$@' '$<'
