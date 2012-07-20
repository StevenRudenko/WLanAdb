#include "worker.h"

#include "io_compatibility.h"

Worker::Worker(QObject *parent) :
    QObject(parent), SCREEN_WIDTH(io_compatibility::getConsoleWidth()), qout(stdout)
{
}

Worker::~Worker() {
}
