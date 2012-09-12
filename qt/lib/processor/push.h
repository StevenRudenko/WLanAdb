#ifndef PUSHPROCESSOR_H
#define PUSHPROCESSOR_H

#include "adbprocessor.h"

class Push : public AdbProcessor
{
    Q_OBJECT

public:
    Push(Command* cmd = NULL);
    virtual ~Push();

public:
    virtual bool start(P2PClient *p2pClient);

signals:
    void onFileSendingStarted(const QString& filename);
    void onFileSendingProgress(const QString& filename, qint64 sent, qint64 total);
    void onFileSendingEnded(const QString& filename);

};

#endif // PUSHPROCESSOR_H
