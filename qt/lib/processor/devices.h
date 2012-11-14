#ifndef DEVICES_H
#define DEVICES_H

#include <QHash>

#include "adbprocessor.h"

#include "network/broadcastserver.h"
#include "data/client.pb.h"

using namespace com::wlanadb::data;

class Devices : public AdbProcessor
{
    Q_OBJECT

public:
    Devices(Command* cmd = NULL);
    virtual ~Devices();

public:
    virtual bool start(P2PClient *);
    void searchClients(int port, int tries);
    void searchClients(int port, int tries, const QString& serialNumber);

signals:
    void onClientSearchCompleted(QList<Client> const &clients);

private slots:

    void onMessageRequested();
    void onMessageRecieved(const QByteArray& data);

private:
    BroadcastServer* broadcast;
    QHash<QString, Client> clients;

    int tries;
    int requestsSent;
    QString clientSerialNumber;
};

#endif // DEVICES_H
