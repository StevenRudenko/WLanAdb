#include "wlancat.h"

#include "io_compatibility.h"
#include "utils.h"

#include "./data/message.pb.h"

#include "./worker/pushworker.h"
#include "./worker/logcatworker.h"
#include "./worker/installworker.h"

using namespace std;
using namespace com::wlancat::data;

namespace {

const int BROADCAST_PORT = 44533;
const int MAX_REQUESTS_SENT = 5;

}

WLanCat::WLanCat(int argc, char *argv[]) :
    QObject(NULL), p2pClient(NULL), qout(stdout), worker(NULL), requestsSent(0)
{
    if (argc < 2) {
        //TODO: add help output
        qout << tr("There is not enough arguments were passed.") << endl;
        exit(0);
    }

    cmd.set_command(argv[1]);
    qout << "Command: " << argv[1] << endl;
    qout << "Arguments count: " << argc << endl;
    for (int i=2; i<argc; ++i) {
        qout << "Argument " << i << ": " << argv[i] << endl;
        *cmd.add_params() = argv[i];
    }



    // Verify that the version of the library that we linked against is
    // compatible with the version of the headers we compiled against.
    GOOGLE_PROTOBUF_VERIFY_VERSION;

    broadcast = new BroadcastServer(BROADCAST_PORT);

    connect(broadcast, SIGNAL(onDatagramSent()), this, SLOT(onMessageRequested()));
    connect(broadcast, SIGNAL(onDatagramRecieved(const QByteArray&)), this, SLOT(onMessageRecieved(const QByteArray&)));

    Message msg;
    msg.set_type(Message::REQEST);
    QByteArray datagram(msg.SerializeAsString().c_str(), msg.ByteSize());

    broadcast->start(datagram);
}

WLanCat::~WLanCat()
{
    if (broadcast != NULL) {
        broadcast->stop();
        delete broadcast;
        broadcast = NULL;
    }

    if (p2pClient != NULL) {
        p2pClient->disconnectFromServer();
        delete p2pClient;
        p2pClient = NULL;
    }

    if (worker != NULL) {
        delete worker;
        worker = NULL;
    }

    // Optional:  Delete all global objects allocated by libprotobuf.
    google::protobuf::ShutdownProtobufLibrary();

}

void WLanCat::onMessageRequested()
{
    if (MAX_REQUESTS_SENT == requestsSent) {
        broadcast->stop();
        delete broadcast;
        broadcast = 0;

        if (clients.isEmpty()) {
            qout << tr("\rThere is no any client available to connect with.") << endl;
            qout << tr("Please check Google Play Store site for client details: http://...") << endl;
            exit(0);
            return;
        }

        selectClient();
        return;
    }

    ++requestsSent;

    qout << tr("\rSearching for devices");
    for (int i=0; i<requestsSent; ++i)
        qout << ".";
    qout.flush();
}

void WLanCat::onMessageRecieved(const QByteArray& data)
{
    Message msg;
    msg.ParseFromArray(data.data(), data.size());
    if (msg.type() != Message::RESPONSE)
        return;

    Client client = msg.client();
    if (client.ip().empty())
        return;

    const QString& clientIp = QString::fromStdString(client.ip());
    if (clients.contains(clientIp))
        return;

    clients.insert(clientIp, client);
}

void WLanCat::selectClient()
{
    int size = clients.size();
    if (size == 1) {
        client = clients.begin().value();
        connectToClient();
        return;
    }

    QHashIterator<QString, Client> i(clients);
    uint index = 0;

    qout << tr("\rThere are %1 devices found:").arg(size) << endl;
    while (i.hasNext()) {
        i.next();

        ++index;

        Client client = i.value();
        const QString& clientName = QString::fromStdString(client.name());
        qout << tr("%1) %2 - %3 (%4)").arg(QString::number(index), clientName, i.key(), QString::number(client.port())) << endl;
    }
    qout << tr("Please select device [1..%1]:").arg(QString::number(size)) << endl;

    int selection;
    QTextStream qin(stdin);

    qin >> selection;

    if (selection > 0 && selection <= size) {
        client = clients.values().at(--selection);
        connectToClient();
    } else {
        selectClient();
    }
}

void WLanCat::connectToClient() {
    if (p2pClient != 0)
        delete p2pClient;

    p2pClient = new P2PClient();

    connect(p2pClient, SIGNAL(connected()), this, SLOT(onConnectedToClient()));
    connect(p2pClient, SIGNAL(disconnected()), this, SLOT(onDisconnectedFromClient()));

    const QString clientName = QString::fromStdString(client.name());
    const QString clientIp = QString::fromStdString(client.ip());

    qout << tr("\rTrying connect to %1 - %2 (%3)").arg(clientName, clientIp, QString::number(client.port()));

    p2pClient->connectToServer(clientIp, client.port());
}

void WLanCat::onConnectedToClient()
{
    const QString clientName = QString::fromStdString(client.name());
    const QString clientIp = QString::fromStdString(client.ip());

    qout << tr("\rConnected to %1 - %2 (%3)").arg(clientName, clientIp, QString::number(client.port())) << endl;
    qout.flush();

    if (client.use_pin()) {
        qout << tr("Client requests PIN to access its log. Please enter PIN:") << endl;

        QString pin;
        QTextStream qin(stdin);

        io_compatibility::setInputEcho(false);
        pin = qin.readLine();
        io_compatibility::setInputEcho(true);

        // encoding pin
        pin = utils::getHash(pin);
        cmd.set_pin(pin.toStdString());
    }

    QByteArray data;
    QDataStream request(&data, QIODevice::WriteOnly);

    QString command = QString::fromUtf8(cmd.command().c_str());
    if (0 == command.compare("push") || 0 == command.compare("install")) {
        if (0 == command.compare("push"))
            worker = new PushWorker();
        else
            worker = new InstallWorker();
        worker->getCommand(cmd);

        request << cmd.ByteSize();
        request.writeRawData(cmd.SerializeAsString().c_str(), cmd.ByteSize());
        p2pClient->send(data);

        connect(p2pClient, SIGNAL(onFileSendingStarted(const QString&)), worker, SLOT(onFileSendingStarted(const QString&)));
        connect(p2pClient, SIGNAL(onFileSendingProgress(const QString&,qint64,qint64)), worker, SLOT(onFileSendingProgress(const QString&,qint64,qint64)));
        connect(p2pClient, SIGNAL(onFileSendingEnded(const QString&)), worker, SLOT(onFileSendingEnded(const QString&)));

        QString filename = QString::fromUtf8(cmd.params(0).c_str());
        if (!p2pClient->sendFile(filename)) {
            qout << tr("Fail to open file: %1").arg(filename) << endl;
            exit(0);
        }
    } else if (0 == command.compare("logcat")) {
        worker = new LogcatWorker();
        request << cmd.ByteSize();
        request.writeRawData(cmd.SerializeAsString().c_str(), cmd.ByteSize());

        p2pClient->send(data);

        connect(p2pClient, SIGNAL(onDataRecieved(const QString&)), worker, SLOT(onLogLine(const QString&)));
    } else {
        //TODO: show help message here
        qout << tr("Unknown command: ") << cmd.command().c_str() << endl;
        exit(0);
    }
}


void WLanCat::onDisconnectedFromClient()
{
    qout << endl;// << tr("Connection with client was closed") << endl;
    exit(0);
}
