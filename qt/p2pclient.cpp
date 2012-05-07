#include "p2pclient.h"

P2PClient::P2PClient(QObject *parent) :
    QObject(parent), in(0)
{

    tcpSocket = new QTcpSocket();

    connect(tcpSocket, SIGNAL(connected()), this, SLOT(connectedToServer()));
    connect(tcpSocket, SIGNAL(disconnected()), this, SLOT(connectionClosedByServer()));
    connect(tcpSocket, SIGNAL(readyRead()), this, SLOT(read()));
    connect(tcpSocket, SIGNAL(error(QAbstractSocket::SocketError)), this, SLOT(error()));
}

P2PClient::~P2PClient()
{
    disconnectFromServer();
    if (tcpSocket != 0) {
        delete tcpSocket;
        tcpSocket = 0;
    }
}

void P2PClient::connectToServer(const QString& server, int port, const QByteArray& request)
{
    this->request = request;
    tcpSocket->connectToHost(server, port);
}

void P2PClient::disconnectFromServer()
{
    if (in != 0) {
        delete in;
        in = 0;
    }

    tcpSocket->close();

    disconnected();
}

void P2PClient::connectedToServer()
{
    tcpSocket->setSocketOption(QAbstractSocket::KeepAliveOption, 1);
    tcpSocket->setSocketOption(QAbstractSocket::LowDelayOption, 1);
    tcpSocket->setReadBufferSize(4096);

    connected();

    tcpSocket->write(request);
    tcpSocket->flush();
}

void P2PClient::read()
{
    if (in == 0) {
        in = new QTextStream(tcpSocket);
        in->setCodec("UTF-8");
    }

    while (!in->atEnd()) {
        QString line = in->readLine();
        if (line.isEmpty())
            break;

        line.append("\r\n");
        onDataRecieved(line);
    }
}

void P2PClient::connectionClosedByServer()
{
    //qDebug() << "Error: Connection closed by server";
    disconnectFromServer();
}

void P2PClient::error()
{
    //qDebug() << "Error: Connection closed by error";
    disconnectFromServer();
}

