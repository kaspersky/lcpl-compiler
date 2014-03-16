all: build

.PHONY: test

build:
	javac -cp "lib/*" -d ./bin -sourcepath ./src ./src/LCPL*.java ./src/ro/pub/cs/lcpl/*.java

run:
	java -cp "bin/:lib/snakeyaml-1.13.jar" LCPLCodeGen test.run test.ir

clean:
	rm -rf ./bin/* *.o *.ir *.s *.run *.err *.out *~

test:
	java -jar LCPLParser.jar test.lcpl test.ast
	java -jar LCPLSemant.jar test.ast test.run

check:
	llc test.ir
	gcc -g test.ir.s -c
	gcc -g lcpl_runtime.c -c
	gcc -g test.ir.o lcpl_runtime.o -o test

jar: clean build
	echo "Main-Class: LCPLCodeGen" > bin/manifest.txt
	(cd bin && jar xf ../lib/snakeyaml-1.13.jar)
	jar cfm ../LCPLCodeGen.jar bin/manifest.txt  -C bin .
