.PHONY: build run test clean

build:
	mkdir -p build/classes
	find src/main/java -name '*.java' > build/sources.txt
	javac --release 17 -Xlint:all -Werror -d build/classes @build/sources.txt

run: build
	java -cp build/classes shop.Main

test: build
	mkdir -p build/test-classes
	find src/test/java -name '*.java' > build/test-sources.txt
	javac --release 17 -Xlint:all -Werror -cp build/classes -d build/test-classes @build/test-sources.txt
	java -cp build/classes:build/test-classes shop.OrderFacadeTest

clean:
	rm -rf build
