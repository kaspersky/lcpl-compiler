all: build

build:
	java -jar lib/antlr-4.1-complete.jar -no-listener -visitor src/ex.g
	javac -cp "lib/*" -sourcepath ./src/ ./src/*.java -d ./bin

tree:
	java -cp "./bin/:/:lib/*" org.antlr.v4.runtime.misc.TestRig ex program -gui test/hello.lcpl

clean:
	rm -rf ./bin/* src/exBaseVisitor.java src/exLexer.java src/exLexer.tokens src/exParser.java src/ex.tokens src/exVisitor.java bonus.out bonus.run

bonus:
	java -cp "lib/*:./bin" LCPLParser test/bonus.lcpl bonus.out
	java -jar sem.jar bonus.out bonus.run
	java -jar sem.jar bonus.run --run
