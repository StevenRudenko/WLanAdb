#ifndef WLANCAT_H
#define WLANCAT_H

#include <QObject>
#include <QRegExp>
#include <QString>
#include <QTextStream>

#include "broadcastserver.h"
#include "client.pb.h"
#include "logwriter.h"
#include "p2pclient.h"

using namespace com::wlancat::data;

class WLanCat : public QObject
{
    Q_OBJECT
public:
    explicit WLanCat(QObject *parent = 0);
    virtual ~WLanCat();

signals:


private slots:

    void onMessageRequested();
    void onMessageRecieved(const QByteArray& data);

    void onDisconnectedFromClient();

private:
    void selectClient();
    void readLogsFromClient(Client& client);

private:
    BroadcastServer* broadcast;
    P2PClient* p2pClient;

    QTextStream qout;
    LogWriter logWriter;

    QHash<QString, Client> clients;
    int requestsSent;
};

#endif // WLANCAT_H
