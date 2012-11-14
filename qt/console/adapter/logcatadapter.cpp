#include "logcatadapter.h"

#include "utils/io_compatibility.h"
#include "utils/myconfig.h"

namespace {

const int TIME_WIDTH = -1;

const int TAGTYPE_WIDTH = 1;
const int TAG_WIDTH = 20;
const int PROCESS_WIDTH = 5;
const int HEADER_WIDTH = TIME_WIDTH + 1 + TAGTYPE_WIDTH + 1 + TAG_WIDTH + 1 + PROCESS_WIDTH + 1;

const QString TYPES = "VDIWE";
const int TYPE_V = 0;
const int TYPE_D = 1;
const int TYPE_I = 2;
const int TYPE_W = 3;
const int TYPE_E = 4;

const int COLORS_COUNT = 7;
const int COLORS[] = {
    io_compatibility::FOREGROUND_RED,
    io_compatibility::FOREGROUND_GREEN,
    io_compatibility::FOREGROUND_YELLOW,
    io_compatibility::FOREGROUND_BLUE,
    io_compatibility::FOREGROUND_MAGENTA,
    io_compatibility::FOREGROUND_CYAN,
    io_compatibility::FOREGROUND_WHITE
};

}

LogcatAdapter::LogcatAdapter(QObject *parent) :
    Adapter(parent), nextColor(0)
{
    logRegEx.setPattern("^([A-Z])\\/(.*)\\(\\s*(\\d+)\\s*\\): (.*)$");

    tagsList.insert("dalvikvm", 3);
    tagsList.insert("Process", 3);
    tagsList.insert("ActivityManager", 5);
    tagsList.insert("ActivityThread", 5);
}

LogcatAdapter::~LogcatAdapter() {
    tagsList.clear();
}

void LogcatAdapter::onLogLine(const QString& str)
{
    if (logRegEx.indexIn(str) == -1) {
        qout << "WARNING: there is unknown string format: " << str << endl;
        return;
    }

    if (MyConfig::CLEAR_STYLE) {
        qout << str << endl;
        return;
    }

    printProcess(logRegEx.cap(3));
    io_compatibility::setTextColor(qout, io_compatibility::RESET, io_compatibility::RESET);
    qout << " ";
    printTagType(logRegEx.cap(1));
    io_compatibility::setTextColor(qout, io_compatibility::RESET, io_compatibility::RESET);
    qout << " ";
    printTag(logRegEx.cap(2));
    io_compatibility::setTextColor(qout, io_compatibility::RESET, io_compatibility::RESET);
    qout << " ";
    printMessage(logRegEx.cap(4));
}

void LogcatAdapter::printTagType(const QString& typeString) {
    int type = TYPES.indexOf(typeString);
    switch (type) {
    case TYPE_V:
        io_compatibility::setTextColor(qout, io_compatibility::FOREGROUND_WHITE, io_compatibility::BACKGROUND_BLACK);
        break;
    case TYPE_D:
        io_compatibility::setTextColor(qout, io_compatibility::FOREGROUND_BLACK, io_compatibility::BACKGROUND_BLUE);
        break;
    case TYPE_I:
        io_compatibility::setTextColor(qout, io_compatibility::FOREGROUND_BLACK, io_compatibility::BACKGROUND_GREEN);
        break;
    case TYPE_W:
        io_compatibility::setTextColor(qout, io_compatibility::FOREGROUND_BLACK, io_compatibility::BACKGROUND_YELLOW);
        break;
    case TYPE_E:
        io_compatibility::setTextColor(qout, io_compatibility::FOREGROUND_BLACK, io_compatibility::BACKGROUND_RED);
        break;
    }
    qout << TYPES[type];
}

void LogcatAdapter::printProcess(const QString& processString) {
    io_compatibility::setTextColor(qout, io_compatibility::FOREGROUND_WHITE, io_compatibility::BACKGROUND_BLACK);
    qout << processString.rightJustified(PROCESS_WIDTH, ' ');
}

void LogcatAdapter::printTag(const QString& tagString) {
    QString tag = tagString.rightJustified(TAG_WIDTH, ' ', true);
    if (!tagsList.contains(tag)) {
        tagsList.insert(tag, nextColor);
        ++nextColor;
        if (nextColor == 7)
            nextColor = 0;
    }
    int color = tagsList.value(tag);
    io_compatibility::setTextColor(qout, COLORS[color], io_compatibility::IGNORE);
    qout << tag;
}

void LogcatAdapter::printMessage(const QString& messageString) {
    const int wrap_area = SCREEN_WIDTH - HEADER_WIDTH;
    int current = 0;
    const int len = messageString.length();
    while (current < len) {
        int next = qMin(wrap_area, len - current);
        QString part = messageString.mid(current, next);
        if (current > 0)
            qout << QString(HEADER_WIDTH, ' ');
        qout << part << endl;
        current += next;
    }
}
