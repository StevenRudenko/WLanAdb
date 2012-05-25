#ifdef WIN32
#include <windows.h>
#else
#include <sys/ioctl.h>
#include <termios.h>
#include <unistd.h>
#endif

#include "io_compatibility.h"
#include <QTextStream>

namespace io_compatibility {

void setInputEcho(bool on)
{
#ifdef WIN32
    DWORD  mode;
    HANDLE hConIn = GetStdHandle( STD_INPUT_HANDLE );
    GetConsoleMode( hConIn, &mode );
    mode = on
            ? (mode |   ENABLE_ECHO_INPUT )
            : (mode & ~(ENABLE_ECHO_INPUT));
    SetConsoleMode( hConIn, mode );
#else
    struct termios settings;
    tcgetattr( STDIN_FILENO, &settings );
    settings.c_lflag = on
            ? (settings.c_lflag |   ECHO )
            : (settings.c_lflag & ~(ECHO));
    tcsetattr( STDIN_FILENO, TCSANOW, &settings );
#endif
}

bool setTextColor(QTextStream& qout, int fgColor, int bgColor)
{
#ifdef WIN32
    HANDLE hconsole = GetStdHandle (STD_OUTPUT_HANDLE);
    return (SetConsoleTextAttribute (hconsole, fgColor | bgColor) == TRUE);
#else
    if (fgColor != IGNORE)
        qout << "\033[" << fgColor << "m";
    if (bgColor != IGNORE)
        qout << "\033[" << bgColor << "m";
    return true;
#endif
}

int getConsoleWidth()
{
#ifdef WIN32
    CONSOLE_SCREEN_BUFFER_INFO csbi;
    int ret = GetConsoleScreenBufferInfo(GetStdHandle( STD_OUTPUT_HANDLE ),&csbi);
    if (ret)
        return csbi.dwSize.X;
    return -1;
#else
    struct winsize size;
    ioctl( 0, TIOCGWINSZ, (char *) &size );
    return size.ws_col;
#endif
}

}

