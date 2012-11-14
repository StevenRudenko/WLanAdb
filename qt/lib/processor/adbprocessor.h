#ifndef ADBCOMMAND_H
#define ADBCOMMAND_H

#include <QObject>

#include "network/p2pclient.h"
#include "data/command.pb.h"

using namespace com::wlanadb::data;

class AdbProcessor : public QObject
{
    Q_OBJECT

public:
    AdbProcessor(Command * cmd = NULL);
    virtual ~AdbProcessor();

public:
    virtual bool start(P2PClient *p2pClient);

public:
    Command& getCommand() const;

    void setPin(const QString &pin);

private:
    Command* cmd;
};

#endif // ADBCOMMAND_H
