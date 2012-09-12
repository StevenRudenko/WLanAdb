#include "logcat.h"

Logcat::Logcat(Command *cmd) :
    AdbProcessor(cmd)
{
}

Logcat::~Logcat()
{
}

bool Logcat::start(P2PClient *p2pClient)
{
    connect(p2pClient, SIGNAL(onDataRecieved(const QString&)), this, SLOT(logLine(const QString&)));

    AdbProcessor::start(p2pClient);
    return true;
}

void Logcat::logLine(const QString &str)
{
    emit onLogLine(str);
}
