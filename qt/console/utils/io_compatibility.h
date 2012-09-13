#ifndef IO_COMPATIBILITY_H
#define IO_COMPATIBILITY_H

#include <QTextStream>

namespace io_compatibility {

#ifndef WIN32
const int IGNORE                = -1;
const int RESET                 = 0;

const int FOREGROUND_BLACK      = 30;
const int FOREGROUND_RED        = 31;
const int FOREGROUND_GREEN      = 32;
const int FOREGROUND_YELLOW     = 33;
const int FOREGROUND_BLUE       = 34;
const int FOREGROUND_MAGENTA    = 35;
const int FOREGROUND_CYAN       = 36;
const int FOREGROUND_WHITE      = 37;

const int BACKGROUND_BLACK      = 40;
const int BACKGROUND_RED        = 41;
const int BACKGROUND_GREEN      = 42;
const int BACKGROUND_YELLOW     = 43;
const int BACKGROUND_BLUE       = 44;
const int BACKGROUND_MAGENTA    = 45;
const int BACKGROUND_CYAN       = 46;
const int BACKGROUND_WHITE      = 47;
#else
// all constants will be defined by including <windows.h>
/*
const int IGNORE                = -1;
const int RESET                 = 7;

const int FOREGROUND_INTENSITY  = 0x0008; // foreground color is intensified
const int FOREGROUND_BLACK      = 0;
const int FOREGROUND_RED        = FOREGROUND_RED;
const int FOREGROUND_GREEN      = FOREGROUND_GREEN;
const int FOREGROUND_YELLOW     = FOREGROUND_RED   | FOREGROUND_GREEN | FOREGROUND_INTENSITY;
const int FOREGROUND_BLUE       = FOREGROUND_BLUE;
const int FOREGROUND_MAGENTA    = FOREGROUND_RED   | FOREGROUND_BLUE;
const int FOREGROUND_CYAN       = FOREGROUND_GREEN | FOREGROUND_BLUE;
const int FOREGROUND_WHITE      = FOREGROUND_RED   | FOREGROUND_GREEN | FOREGROUND_BLUE | FOREGROUND_INTENSITY;

const int BACKGROUND_INTENSITY  = 0x0080; // background color is intensified.
const int BACKGROUND_BLACK      = 0;
const int BACKGROUND_RED        = BACKGROUND_RED;
const int BACKGROUND_GREEN      = BACKGROUND_GREEN;
const int BACKGROUND_YELLOW     = BACKGROUND_RED   | BACKGROUND_GREEN | BACKGROUND_INTENSITY;
const int BACKGROUND_BLUE       = BACKGROUND_BLUE;
const int BACKGROUND_MAGENTA    = BACKGROUND_RED   | BACKGROUND_BLUE;
const int BACKGROUND_CYAN       = BACKGROUND_GREEN | BACKGROUND_BLUE;
const int BACKGROUND_WHITE      = BACKGROUND_RED   | BACKGROUND_GREEN | BACKGROUND_BLUE | BACKGROUND_INTENSITY;
*/
#endif

void setInputEcho(bool on = true);

bool setTextColor(QTextStream& qout, int fgColor, int bgColor);

int getConsoleWidth();

}
#endif // IOCOMPATIBILITY_H
