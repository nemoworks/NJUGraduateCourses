#ifndef __TURING_MACHINE_H__
#define __TURING_MACHINE_H__
#include <string>
#include <vector>
#include <map>
#include "Tape.h"
#include "Transition.h"

using namespace std;

class TuringMachine
{
private:
    int numOfTapes;
    int stepCount;
    char blank;
    string startState;
    string currentState;
    vector<string> states;
    vector<string> finalStates;
    vector<char> inputSymbols;
    vector<char> tapeSymbols;
    vector<Transition> transitions;
    vector<Tape> tapes;
    vector<int> heads;
    

    void setAttr(string attr, string value);
    void addTransition(string line);
public:
    TuringMachine(string tmConfig);
    bool step();
    string getID();
    string toString();
    void refresh();
    bool input(string str);
    bool accept();
    string result();
};

#endif