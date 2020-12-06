#include "Tape.h"
#include "utils.h"
#include <assert.h>
#include <iostream>
using namespace std;

char Tape::get(int index)
{
    if (index >= 0)
    {
        if (index >= this->content.size())
        {
            return this->blank;
        }
        return this->content[index];
    }
    else
    {
        index = -index - 1;
        if (index >= this->leftContent.size())
        {
            return this->blank;
        }
        return this->leftContent[index];
    }
}

void Tape::set(int index, char symbol)
{
    if (index >= 0)
    {
        if (index < this->content.size())
        {
            this->content[index] = symbol;
            return;
        }
        else if (index == this->content.size())
        {
            this->content.push_back(symbol);
            return;
        }
        assert(0);
    }
    else
    {
        index = -index - 1;
        if (index < this->leftContent.size())
        {
            this->leftContent[index] = symbol;
            return;
        }
        else if (index == this->leftContent.size())
        {
            this->leftContent.push_back(symbol);
            return;
        }
        assert(0);
    }
}

string Tape::toString(int head)
{
    string res = "";
    res = res + "Index" + int_to_string(this->index) + "\t:\t";
    int first = 0, last = 0;

    for (int i = this->leftContent.size() - 1; i >= 0; i--)
    {
        if (this->leftContent[i] != this->blank)
        {
            first = -(i + 1);
            break;
        }
    }
    if (first == 0)
    {
        for (int i = 0; i < this->content.size(); i++)
        {
            if (this->content[i] != this->blank)
            {
                first = i;
                break;
            }
            if (i == head)
            {
                first = head;
                break;
            }
        }
    }

    for (int i = this->content.size() - 1; i >= 0; i--)
    {
        if (this->content[i] != this->blank)
        {
            last = i;
            break;
        }
    }
    if (last == 0)
    {
        for (int i = 0; i < this->leftContent.size(); i++)
        {
            if (this->leftContent[i] != this->blank)
            {
                last = -(i + 1);
                break;
            }
            if (i == head)
            {
                last = head;
                break;
            }
        }
    }

    if (head < first)
    {
        first = head;
    }
    else if (head > last)
    {
        last = head;
    }
    for (int i = first; i <= last; i++)
    {
        res = res + int_to_string(i) + "\t";
    }
    res = res + "\n";
    res = res + "Tape" + int_to_string(this->index) + "\t:\t";
    for (int i = first; i <= last; i++)
    {
        res = res + this->get(i) + "\t";
    }
    res = res + "\n";

    res = res + "Head" + int_to_string(this->index) + "\t:\t";
    int offset = head - first;
    for (int i = 0; i < offset; i++)
    {
        res = res + " " + "\t";
    }
    res = res + "^\n";

    return res;
}

void Tape::setBlank(char ch) { this->blank = ch; }

void Tape::setIndex(int index) { this->index = index; }

void Tape::init(string input)
{
    for (int i = 0; i < input.length(); i++)
    {
        this->content.push_back(input[i]);
    }
}

string Tape::result()
{
    string result = "";
    bool append = false;
    int first = -(this->leftContent.size() + 1);
    int last = this->content.size() - 1;
    for (int i = first; i <= last; i++)
    {
        char ch = this->get(i);
        if (ch != this->blank)
        {
            append = true;
        }
        if (append)
        {
            result += ch;
        }
    }
    string temp = "";
    int begin = result.find_first_not_of(this->blank);
    int end = result.find_last_not_of(this->blank);
    //cout << result <<" "<<begin << " "<<end <<endl;
    result = result.substr(begin, end - begin + 1);
    return result;
}