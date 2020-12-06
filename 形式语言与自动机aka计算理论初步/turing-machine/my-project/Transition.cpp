#include "Transition.h"
#include <sstream>
using namespace std;
Transition::Transition(string description)
{
    stringstream ss(description);
    ss >> this->state >> this->tapeSymbols >> this->newSymbols >> this->directions >> this->newState;
}

string Transition::toString()
{
    return this->state + " " + this->tapeSymbols + " " + this->newSymbols + " " + this->directions + " " + this->newState + "\n";

}

bool symbolsMatch(string str1, string str2)
{
    if(str1.length() != str2.length())
    {
        return false;
    }

    for(int i = 0; i < str1.length(); i++)
    {
        if(str1[i] == '*' || str2[i] == '*' || str1[i] == str2[i]) {
            continue;
        }
        else {
            return false;
        }
    }

    return true;
}

bool Transition::match(string state, string tapeSymbols)
{
    if(state != this->state)
    {
        return false;
    }
    return symbolsMatch(this->tapeSymbols, tapeSymbols);
}

string Transition::getNewSymbols()
{
    return this->newSymbols;
}

string Transition::getNewState()
{
    return this->newState;
}

string Transition::getDirections()
{
    return this->directions;
}