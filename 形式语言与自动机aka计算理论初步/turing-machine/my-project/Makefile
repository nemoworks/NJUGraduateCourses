CC = g++
CPPFLAGS = -std=c++11

CPPFILES = $(shell find ./ -name "*.cpp")
OBJS = $(CPPFILES:.cpp=.o)


turing: 
	$(CC) -c $(CPPFILES) 
	$(CC) $(CPPFLAGS) $(OBJS) -o turing


.PHONY: clean test case1 case2

clean: 
	rm -f turing
	rm -f *.o

test: clean turing
	./turing ../test

case1: clean turing
	./turing ../case1

case2: clean turing
	./turing ../case2

