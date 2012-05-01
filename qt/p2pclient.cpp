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
    delete tcpSocket;
}

void P2PClient::connectToServer(const QString& server, int port)
{
    tcpSocket->connectToHost(server, port);
    //qDebug() << "Connecting to " << server << " on " << port;
}

void P2PClient::disconnectFromServer()
{
    if (in != 0) {
        delete in;
    }

    tcpSocket->close();

    disconnected();
}

void P2PClient::connectedToServer()
{
    tcpSocket->setSocketOption(QAbstractSocket::KeepAliveOption, 1);
    tcpSocket->setSocketOption(QAbstractSocket::LowDelayOption, 1);
    tcpSocket->setReadBufferSize(1024);

    connected();

    /*
    QByteArray block;
    QDataStream out(&block, QIODevice::WriteOnly);
    out.setVersion(QDataStream::Qt_4_3);

    out << quint16(0) << quint8('S') << fromComboBox->currentText()
        << toComboBox->currentText() << dateEdit->date()
        << timeEdit->time();

    if (departureRadioButton->isChecked()) {
        out << quint8('D');
    } else {
        out << quint8('A');
    }
    out.device()->seek(0);
    out << quint16(block.size() - sizeof(quint16));
    tcpSocket.write(block);
    */
}

void P2PClient::read()
{
    bool firstRead = in == 0;
    if (in == 0) {
        in = new QTextStream(tcpSocket);
        in->setCodec("UTF-8");
    }

    while (!in->atEnd()) {
        QString line = in->readLine();
        if (line.isEmpty())
            break;

        if (in->atEnd()) {
            onDataRecieved(line);
            break;
        }

        if (!firstRead)
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

