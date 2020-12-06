#include "TuringMachine.h"
#include "utils.h"
#include <fstream>
#include <iostream>
#include <sstream>
#include <stdlib.h>
#include <string.h>
#include <algorithm>
#include <assert.h>
using namespace std;

string removeSpaces(string line)
{
    string res = "";
    bool flag = false;
    for (int i = 0; i < line.length(); i++)
    {
        if (line[i] != ' ')
        {
            flag = true;
        }
        if (flag)
        {
            res.push_back(line[i]);
        }
    }
    return res;
}

string removeComment(string line)
{
    int semiPos = line.find_last_of(';');
    //cout << semiPos << endl;
    if (semiPos != -1)
    {
        //有注释，要去掉
        line = line.substr(0, semiPos);
    }
    return line;
}

vector<string> splitByComma(string str)
{
    vector<string> res;
    int p = 0;
    char buffer[str.length()];
    for (int i = 0; i < str.length(); i++)
    {
        if (str[i] != ',')
        {
            buffer[p++] = str[i];
        }
        else
        {
            buffer[p] = '\0';
            string temp = buffer;
            res.push_back(temp);
            p = 0;
        }
    }
    buffer[p] = '\0';
    string temp = buffer;
    res.push_back(temp);
    return res;
}

TuringMachine::TuringMachine(string tmConfig)
{
    this->stepCount = 0;
    ifstream fin(tmConfig.c_str());
    string line;
    vector<string> buffer;
    while (getline(fin, line))
    {
        line = removeSpaces(line);
        line = removeComment(line);
        if (line.length() > 0)
        {
            //cout << line << endl;
            buffer.push_back(line);
        }
    }
    fin.close();
    for (int i = 0; i < buffer.size(); i++)
    {
        string line = buffer[i];
        if (line[0] == '#')
        {
            stringstream ss(line);
            string attr;
            string assign;
            string value;
            ss >> attr >> assign >> value;
            this->setAttr(attr, value);
        }
        else
        {
            //转移函数
            this->addTransition(line);
        }
    }
}

void TuringMachine::addTransition(string line)
{
    //cout << "Add transition function: " << line << endl;
    this->transitions.push_back(Transition(line));
}

void TuringMachine::setAttr(string attr, string value)
{
    //cout << "Attribute: " << attr << endl;
    //cout << "Value: " << value << endl;
    if (attr == "#Q")
    {
        int first = value.find_first_of('{');
        int last = value.find_last_of('}');
        if (first == -1 || last == -1)
        {
            printf("Wrong value\n");
        }
        value = value.substr(first + 1, last - first - 1);
        vector<string> states = splitByComma(value);
        for (int i = 0; i < states.size(); i++)
        {
            this->states.push_back(states[i]);
        }
    }
    else if (attr == "#S")
    {
        int first = value.find_first_of('{');
        int last = value.find_last_of('}');
        if (first == -1 || last == -1)
        {
            printf("Wrong value\n");
        }
        value = value.substr(first + 1, last - first - 1);
        vector<string> symbols = splitByComma(value);
        for (int i = 0; i < symbols.size(); i++)
        {
            this->inputSymbols.push_back(symbols[i][0]);
        }
    }
    else if (attr == "#G")
    {
        int first = value.find_first_of('{');
        int last = value.find_last_of('}');
        if (first == -1 || last == -1)
        {
            printf("Wrong value\n");
        }
        value = value.substr(first + 1, last - first - 1);
        vector<string> symbols = splitByComma(value);
        for (int i = 0; i < symbols.size(); i++)
        {
            this->tapeSymbols.push_back(symbols[i][0]);
        }
    }
    else if (attr == "#q0")
    {
        this->startState = value;
        this->currentState = value;
    }
    else if (attr == "#B")
    {
        this->blank = value[0];
        for (int i = 0; i < this->tapes.size(); i++)
        {
            this->tapes[i].setBlank(this->blank);
        }
    }
    else if (attr == "#F")
    {
        int first = value.find_first_of('{');
        int last = value.find_last_of('}');
        if (first == -1 || last == -1)
        {
            printf("Wrong value\n");
        }
        value = value.substr(first + 1, last - first - 1);
        vector<string> states = splitByComma(value);
        for (int i = 0; i < states.size(); i++)
        {
            this->finalStates.push_back(states[i]);
        }
    }
    else if (attr == "#N")
    {
        this->numOfTapes = atoi(value.c_str());
        for (int i = 0; i < this->numOfTapes; i++)
        {
            this->heads.push_back(0);
            Tape tape;
            tape.setBlank(this->blank);
            tape.setIndex(i);
            this->tapes.push_back(tape);
        }
    }
}

string TuringMachine::getID()
{
    string res = "";
    res = res + "Step\t:\t" + int_to_string(this->stepCount) + "\n";
    for (int i = 0; i < this->tapes.size(); i++)
    {
        res = res + this->tapes[i].toString(this->heads[i]);
    }
    res = res + "State\t:\t" + this->currentState + "\n";
    res = res + "-----------------------------------------------------------------\n";
    return res;
}

string TuringMachine::toString()
{
    string res = "";
    res = res + "Num of tapes: " + int_to_string(this->numOfTapes) + "\n";
    res = res + "Blank symbol: " + this->blank + "\n";
    res = res + "States: ";
    for (int i = 0; i < this->states.size(); i++)
    {
        res = res + this->states[i] + ",";
    }
    res = res + "\n";
    res = res + "Start state: " + this->startState + "\n";
    res = res + "Final states: ";
    for (int i = 0; i < this->finalStates.size(); i++)
    {
        res = res + this->finalStates[i] + ",";
    }
    res = res + "\n";
    res = res + "Input symbols: ";
    for (int i = 0; i < this->inputSymbols.size(); i++)
    {
        res = res + this->inputSymbols[i] + ",";
    }
    res = res + "\n";
    res = res + "Tape symbols: ";
    for (int i = 0; i < this->tapeSymbols.size(); i++)
    {
        res = res + this->tapeSymbols[i] + ",";
    }
    res = res + "\n";
    res = res + "Heads: ";
    for (int i = 0; i < this->heads.size(); i++)
    {
        res = res + int_to_string(this->heads[i]) + ',';
    }
    res = res + "\n";
    res = res + "Transitions: \n";
    for (int i = 0; i < this->transitions.size(); i++)
    {
        res = res + this->transitions[i].toString();
    }
    res = res + "Tapes: \n";
    for (int i = 0; i < this->tapes.size(); i++)
    {
        res = res + this->tapes[i].toString(heads[i]) + "\n";
    }
    return res;
}

void TuringMachine::refresh()
{
    this->currentState = this->startState;
    this->tapes = vector<Tape>();
    for(int i = 0; i < this->numOfTapes; i++) {
        this->heads[i] = 0;
        Tape tape;
        tape.setBlank(this->blank);
        tape.setIndex(i);
        this->tapes.push_back(tape);
    }
}

bool TuringMachine::input(string str)
{
    if (this->tapes.size() < 1)
    {
        cout << "No tapes" << endl;
        return false;
    }
    for (int i = 0; i < str.length(); i++)
    {
        bool legal = false;
        char ch = str[i];
        for(int j = 0; j < this->inputSymbols.size(); j++)
        {
            if(ch == this->inputSymbols[j])
            {
                legal = true;
                break;
            }
        }
        if(!legal) {
            return false;
        }
    }
    this->tapes[0].init(str);
    return true;
}

//图灵机的一步转移
//返回true，表示尚未结束，返回false，表示到达了终止状态
bool TuringMachine::step()
{
    if(this->accept()) {
        return false;
    }
    this->stepCount++;
    string tapeSymbols = "";
    for (int i = 0; i < this->numOfTapes; i++)
    {
        tapeSymbols = tapeSymbols + this->tapes[i].get(this->heads[i]);
    }
    vector<Transition> matchedTransitions;
    for (int i = 0; i < this->transitions.size(); i++)
    {
        if (this->transitions[i].match(this->currentState, tapeSymbols))
        {
            matchedTransitions.push_back(this->transitions[i]);
        }
    }
    //如果没有匹配的transition
    if(matchedTransitions.size() == 0)
    {
        return false;
    }
    //选择通配符少的匹配的transition规则
    sort(matchedTransitions.begin(), matchedTransitions.end());

    /*for(int i = 0; i < matchedTransitions.size(); i++)
    {
        cout << "Match: " << matchedTransitions[i].toString();
    }*/

    Transition t = matchedTransitions[0];
    string newTapeSymbols = t.getNewSymbols();
    string newState = t.getNewState();
    string directions = t.getDirections();

    assert(this->numOfTapes == newTapeSymbols.length());
    assert(this->numOfTapes == directions.length());

    //改写纸带
    for(int i = 0; i < this->numOfTapes; i++)
    {
        int head = this->heads[i];
        char oldSymbol = this->tapes[i].get(head);
        char newSymbol = newTapeSymbols[i];
        if(newSymbol == '*')
        {
            this->tapes[i].set(head, oldSymbol);
        }
        else {
            this->tapes[i].set(head, newSymbol);
        }

    }

    //移动磁头
    for(int i = 0; i < this->numOfTapes; i++)
    {
        char direction = directions[i];
        if(direction == 'l') {
            this->heads[i] --;
        }
        else if(direction == 'r')
        {
            this->heads[i] ++;
        }
    }

    //设置新状态
    this->currentState = newState;
    return true;
}

bool TuringMachine::accept()
{
    for(int i = 0; i < this->finalStates.size(); i++)
    {
        if(this->currentState == this->finalStates[i])
        {
            return true;
        }
    }
    return false;
}

string TuringMachine::result()
{
    return this->tapes[0].result();
}