#include "wlancat.h"

#include "message.pb.h"

using namespace std;
using namespace com::wlancat::data;

namespace {
    const int MAX_REQUESTS_SENT = 5;
}

WLanCat::WLanCat(QObject *parent) :
    QObject(parent), qout(stdout), requestsSent(0)
{
    // Verify that the version of the library that we linked against is
    // compatible with the version of the headers we compiled against.
    GOOGLE_PROTOBUF_VERIFY_VERSION;

    broadcast = new BroadcastServer(44533);

    connect(broadcast, SIGNAL(onDatagramSent()), this, SLOT(onMessageRequested()));
    connect(broadcast, SIGNAL(onDatagramRecieved(const QByteArray&)), this, SLOT(onMessageRecieved(const QByteArray&)));

    Message msg;
    msg.set_type(Message::REQEST);
    QByteArray datagram(msg.SerializeAsString().c_str(), msg.ByteSize());

    broadcast->start(datagram);
}

WLanCat::~WLanCat()
{
    if (broadcast != 0) {
        broadcast->stop();
        delete broadcast;
    }

    if (p2pClient != 0) {
        p2pClient->disconnectFromServer();
        delete p2pClient;
    }

    // Optional:  Delete all global objects allocated by libprotobuf.
    google::protobuf::ShutdownProtobufLibrary();

}

void WLanCat::onMessageRequested()
{
    if (MAX_REQUESTS_SENT == requestsSent) {
        broadcast->stop();
        delete broadcast;

        if (clients.isEmpty()) {
            qout << tr("\rThere is no any client available to connect with.") << endl;
            qout << tr("You can download it from Google Play Store by link: http://...") << endl;
            exit(0);
            return;
        }

        if (clients.size() == 1) {
            Client client = clients.begin().value();
            readLogsFromClient(client);
        } else {
            selectClient();
        }
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
    QHashIterator<QString, Client> i(clients);
    int size = clients.size();
    uint index = 0;

    qout << tr("\rThere are %1 devices found:").arg(size) << endl;
    while (i.hasNext()) {
        i.next();

        ++index;

        Client client = i.value();
        const QString& clientName = QString::fromStdString(client.name());
        qout << tr("%1) %2 - %3 (%4)").arg(QString(index), clientName, i.key(), QString::number(client.port())) << endl;
    }
    qout << tr("Please select device by typing it number (Default: 1):") << endl;

    int selection;
    QTextStream qin(stdin);

    qin >> selection;

    if (selection > 0 && selection <= size) {
        Client client = clients.values().at(--selection);
        readLogsFromClient(client);
    } else {
        selectClient();
    }
}

void WLanCat::readLogsFromClient(Client &client) {
    const QString clientName = QString::fromStdString(client.name());
    const QString clientIp = QString::fromStdString(client.ip());
    qout << endl << tr("Starting reading logs from %1 - %2 (%3)").arg(clientName, clientIp, QString::number(client.port())) << endl;

    p2pClient = new P2PClient();

    connect(p2pClient, SIGNAL(onDataRecieved(const QString&)), this, SLOT(onLogLine(const QString&)));
    connect(p2pClient, SIGNAL(disconnected()), this, SLOT(onDisconnectedFromClient()));

    int port = client.port();
    p2pClient->connectToServer(clientIp, port);
}

void WLanCat::onLogLine(const QString& str)
{
    qout << str;
    qout.flush();
}

void WLanCat::onDisconnectedFromClient()
{
    qout << endl << tr("Connection with client was closed") << endl;
    exit(0);
}
