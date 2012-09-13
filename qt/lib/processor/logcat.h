#ifndef LOGCAT_H
#define LOGCAT_H

#include "adbprocessor.h"

class Logcat : public AdbProcessor
{
    Q_OBJECT

public:
    Logcat(Command* cmd = NULL);
    virtual ~Logcat();

public:
    virtual bool start(P2PClient *p2pClient);

signals:
    void onLogLine(const QString& str);
};

#endif // LOGCAT_H
