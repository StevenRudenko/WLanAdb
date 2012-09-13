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
    connect(p2pClient, SIGNAL(onDataRecieved(const QString&)), this, SIGNAL(onLogLine(const QString&)));

    AdbProcessor::start(p2pClient);
    return true;
}
