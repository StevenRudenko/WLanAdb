#include "devices.h"

#include <QByteArray>

#include "data/message.pb.h"

using namespace com::wlanadb::data;

Devices::Devices(Command * cmd) :
    AdbProcessor(cmd), broadcast(NULL), requestsSent(0)
{
}

Devices::~Devices() {
    if (broadcast != NULL) {
        broadcast->stop();
        delete broadcast;
        broadcast = NULL;
    }
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

void Devices::searchClients(int port, int tries, const QString &serialNumber)
{
    clientSerialNumber = serialNumber;
    searchClients(port, tries);
}

bool Devices::start(P2PClient *)
{
    return false;
}

void Devices::onMessageRequested()
{
    if (tries == requestsSent) {
        if (NULL != broadcast) {
            broadcast->stop();
            delete broadcast;
            broadcast = NULL;
        }

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

    if (!clientSerialNumber.isEmpty()) {
        const QString clientId = QString::fromStdString(client.id());
        if (0 != clientSerialNumber.compare(clientId))
            return;
    }
    clients.insert(clientIp, client);
}
