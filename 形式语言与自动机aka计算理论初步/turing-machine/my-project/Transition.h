#ifndef __TRANSITION_H__
#define __TRANSITION_H__
#include <vector>
#include <string>
using namespace std;

class Transition
{
private:
    string state;
    string tapeSymbols;
    string newSymbols;
    string directions;
    string newState;
public:
    Transition(string description);
    bool match(string state, string tapeSymbols);
    string getNewSymbols();
    string getNewState();
    string getDirections();
    string toString();

    bool operator < (const Transition& t) //希望优先选择通配符少的规则
    {
        int count1 = 0;
        int count2 = 0;
        for(int i = 0;i < this->tapeSymbols.length(); i++)
        {
            if(this->tapeSymbols[i] == '*')
            {
                count1 ++;
            }
            if(t.tapeSymbols[i] == '*')
            {
                count2 ++;
            }
        }

        return count1 < count2;
    }
};

#endif