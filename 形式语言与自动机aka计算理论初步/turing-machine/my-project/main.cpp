#include <iostream>
#include <fstream>
#include <stdlib.h>
#include <string>
#include <vector>
#include "TuringMachine.h"
using namespace std;

void testTuringMachine(TuringMachine &tm, string input, string result, string console)
{
    ifstream readInput(input.c_str());
    ofstream writeResult(result.c_str()); //默认写覆盖
    ofstream writeConsoleLog(console.c_str());
    vector<string> inputLines;
    string inputLine;
    while (getline(readInput, inputLine))
    {
        inputLines.push_back(inputLine);
    }
    for (int i = 0; i < inputLines.size(); i++)
    {
        inputLine = inputLines[i];
        if (tm.input(inputLine))
        {
            writeConsoleLog << "Input: " << inputLine << endl;
            writeConsoleLog << "======================= RUN =======================" << endl;
            do
            {
                writeConsoleLog << tm.getID();
            } while (tm.step());
            writeConsoleLog <<"Result: "<< tm.result() << endl;
            writeConsoleLog << "======================= END =======================" << endl;
            if(tm.accept()) {
                writeResult << "True" << endl;
            }
            else {
                writeResult << "False" << endl;
            }
        }
        else
        {
            writeConsoleLog << "Input: " << inputLine << endl;
            writeConsoleLog << "======================= ERR =======================" << endl;
            writeConsoleLog << "Input: " << inputLine << " is illegal." << endl;
            writeConsoleLog << "======================= ERR =======================" << endl;
            writeResult << "Error" << endl;
        }
        tm.refresh();
    }
    readInput.close();
    writeConsoleLog.close();
    writeResult.close();
}

int main(int argc, char *argv[])
{
    if (argc < 2)
    {
        cout << "You need to pass the test directory through arguments." << endl;
        exit(-1);
    }
    string dir = argv[1];
    string tmConfig = dir + "/test.tm";
    string input = dir + "/input.txt";
    string result = dir + "/result.txt";
    string console = dir + "/console.txt";

    TuringMachine tm(tmConfig);
    testTuringMachine(tm, input, result, console);
    return 0;
}