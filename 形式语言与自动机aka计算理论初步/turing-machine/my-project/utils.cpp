#include "utils.h"

string int_to_string(int val)
{
    ostringstream ss;
    ss << val;
    return ss.str();
}