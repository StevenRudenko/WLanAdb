#include "adapter.h"

#include "utils/io_compatibility.h"

Adapter::Adapter(QObject *parent) :
    QObject(parent), SCREEN_WIDTH(io_compatibility::getConsoleWidth()), qout(stdout)
{
}

Adapter::~Adapter() {
}
