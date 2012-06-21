#ifndef WLANCAT_H
#define WLANCAT_H

#include <QObject>
#include <QRegExp>
#include <QString>
#include <QTextStream>

#include "broadcastserver.h"
#include "client.pb.h"
#include "command.pb.h"
#include "p2pclient.h"

#include "logcatworker.h"
#include "pushworker.h"

using namespace com::wlancat::data;

class WLanCat : public QObject
{
    Q_OBJECT
public:
    WLanCat(int argc, char *argv[]);
    virtual ~WLanCat();

signals:


private slots:

    void onMessageRequested();
    void onMessageRecieved(const QByteArray& data);

    void onConnectedToClient();
    void onDisconnectedFromClient();

private:
    void selectClient();
    void connectToClient();

private:
    Command cmd;

    BroadcastServer* broadcast;
    P2PClient* p2pClient;

    QTextStream qout;

    LogcatWorker logcatWorker;
    PushWorker pushWorker;

    QHash<QString, Client> clients;
    Client client;
    int requestsSent;
};

#endif // WLANCAT_H
