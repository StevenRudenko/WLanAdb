#include "devices.h"

#include <QByteArray>

#include "./data/message.pb.h"

using namespace com::wlancat::data;

Devices::Devices(Command * cmd) :
    AdbProcessor(cmd), broadcast(NULL), requestsSent(0)
{
    // Verify that the version of the library that we linked against is
    // compatible with the version of the headers we compiled against.
    GOOGLE_PROTOBUF_VERIFY_VERSION;
}

Devices::~Devices() {
    if (broadcast != NULL) {
        broadcast->stop();
        delete broadcast;
        broadcast = NULL;
    }

    // Optional:  Delete all global objects allocated by libprotobuf.
    google::protobuf::ShutdownProtobufLibrary();
}

void Devices::searchClients(int port, int tries) {
    this->tries = tries;

    broadcast = new BroadcastServer(port);

    connect(broadcast, SIGNAL(onDatagramSent()), this, SLOT(onMessageRequested()));
    connect(broadcast, SIGNAL(onDatagramRecieved(const QByteArray&)), this, SLOT(onMessageRecieved(const QByteArray&)));

    Message msg;
    msg.set_type(Message::REQEST);
    QByteArray datagram(msg.SerializeAsString().c_str(), msg.ByteSize());

    broadcast->start(datagram);
}

bool Devices::start(P2PClient *)
{
    return false;
}

void Devices::onMessageRequested()
{
    if (tries == requestsSent) {
        broadcast->stop();
        delete broadcast;
        broadcast = NULL;

        emit onClientSearchCompleted(clients.values());
        return;
    }

    ++requestsSent;
}

void Devices::onMessageRecieved(const QByteArray& data)
{
    Message msg;
    msg.ParseFromArray(data.data(), data.size());
    if (msg.type() != Message::RESPONSE)
        return;

    const Client client = msg.client();
    if (client.ip().empty())
        return;

    const QString clientIp = QString::fromStdString(client.ip());
    if (clients.contains(clientIp))
        return;

    clients.insert(clientIp, client);
}
