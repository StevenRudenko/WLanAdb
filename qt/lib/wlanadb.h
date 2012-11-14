#ifndef WLANADBLIB_H
#define WLANADBLIB_H

#include "processor/adbprocessor.h"
#include "processor/devices.h"

#include "data/client.pb.h"
#include "data/command.pb.h"

#include "network/p2pclient.h"

using namespace com::wlanadb::data;

class WLanAdb : public QObject {

    Q_OBJECT

public:
    WLanAdb();
    virtual ~WLanAdb();

public:

    static AdbProcessor* prepareProcessor(int argc, char *argv[]);

private:

    static Command* parseCommand(int argc, char *argv[]);
    static AdbProcessor* prepareProcessor(Command *cmd);

public:

public:
    void searchClients(int port, int tries);
    void searchClients(int port, int tries, const QString& serialNumber);

    void connectToClient(const Client& client);
    void disconnectFromClient();

    bool start(AdbProcessor *proc);

signals:
    void onClientSearchCompleted(QList<Client> const &clients);
    void onConnectedToClient(const Client &client);
    void onDisconnectedFromClient();

private slots:
    void connectedToClient();

private:
    P2PClient* p2pClient;

    Devices* devices;
    Client client;
};

#endif // WLANADBLIB_H
