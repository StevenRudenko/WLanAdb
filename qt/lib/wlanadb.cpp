#include "wlanadb.h"

#include "processor/logcat.h"
#include "processor/push.h"
#include "processor/install.h"

#include "commands.h"

WLanAdb::WLanAdb() :
    QObject(NULL), p2pClient(NULL), devices(NULL)
{
}

WLanAdb::~WLanAdb()
{
    if (NULL != devices) {
        delete devices;
        devices = NULL;
    }

    disconnectFromClient();
}

Command* WLanAdb::parseCommand(int argc, char *argv[]) {

    if (argc < 2)
        return NULL;

    const QString command(argv[1]);

    if (
            0 != Commands::DEVICES.compare(command)
            && 0 != Commands::LOGCAT.compare(command)
            && 0 != Commands::PUSH.compare(command)
            && 0 != Commands::INSTALL.compare(command)
            )
    {
        return NULL;
    }

    Command* cmd = new Command();
    cmd->set_command(command.toStdString());

    for (int i=2; i<argc; ++i) {
        *cmd->add_params() = argv[i];
    }
    return cmd;
}

AdbProcessor* WLanAdb::prepareProcessor(int argc, char *argv[])
{
    Command* cmd = parseCommand(argc, argv);
    if (NULL == cmd)
        return NULL;

    return prepareProcessor(cmd);
}

AdbProcessor* WLanAdb::prepareProcessor(Command *cmd) {
    QString command = QString::fromUtf8(cmd->command().c_str());
    if (0 == Commands::DEVICES.compare(command))
        return new Devices(cmd);
    else if (0 == Commands::PUSH.compare(command))
        return new Push(cmd);
    else if (0 == Commands::INSTALL.compare(command))
        return new Install(cmd);
    else if (0 == Commands::LOGCAT.compare(command))
        return new Logcat(cmd);
    return NULL;
}

bool WLanAdb::start(AdbProcessor *proc)
{
    if (NULL == p2pClient)
        return false;

    return proc->start(p2pClient);
}

void WLanAdb::searchClients(int port, int tries)
{
    searchClients(port, tries, QString());
}

void WLanAdb::searchClients(int port, int tries, const QString &serialNumber)
{
    if (NULL != devices)
        delete devices;

    devices = new Devices();

    connect(devices, SIGNAL(onClientSearchCompleted(QList<Client>)), this, SIGNAL(onClientSearchCompleted(QList<Client>)));

    devices->searchClients(port, tries, serialNumber);
}

void WLanAdb::connectToClient(const Client& client)
{
    this->client = client;

    if (NULL != p2pClient)
        delete p2pClient;

    p2pClient = new P2PClient();

    connect(p2pClient, SIGNAL(connected()), this, SLOT(connectedToClient()));
    connect(p2pClient, SIGNAL(disconnected()), this, SIGNAL(onDisconnectedFromClient()));

    p2pClient->connectToServer(client.ip().c_str(), client.port());
}

void WLanAdb::disconnectFromClient() {
    if (p2pClient != NULL) {
        p2pClient->disconnectFromServer();
        delete p2pClient;
        p2pClient = NULL;
    }
}

void WLanAdb::connectedToClient() {
    emit onConnectedToClient(client);
}
