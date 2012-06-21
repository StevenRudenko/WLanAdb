#include "p2pclient.h"

namespace {
const int SIZE_BLOCK_FOR_SEND_FILE = 4096;
}

P2PClient::P2PClient(QObject *parent) :
    QObject(parent), in(NULL), readFile(NULL)
{

    tcpSocket = new QTcpSocket();
    tcpSocket->setSocketOption(QAbstractSocket::KeepAliveOption, 1);
    tcpSocket->setSocketOption(QAbstractSocket::LowDelayOption, 1);
    tcpSocket->setReadBufferSize(4096);

    connect(tcpSocket, SIGNAL(connected()), this, SLOT(connectedToServer()));
    connect(tcpSocket, SIGNAL(disconnected()), this, SLOT(connectionClosedByServer()));
    connect(tcpSocket, SIGNAL(readyRead()), this, SLOT(read()));
    connect(tcpSocket, SIGNAL(error(QAbstractSocket::SocketError)), this, SLOT(error()));
}

P2PClient::~P2PClient()
{
    disconnectFromServer();
    if (tcpSocket != NULL) {
        delete tcpSocket;
        tcpSocket = NULL;
    }
}

void P2PClient::connectToServer(const QString& server, int port)
{
    tcpSocket->connectToHost(server, port);
}

void P2PClient::disconnectFromServer()
{
    if (in != NULL) {
        delete in;
        in = NULL;
    }

    if (readFile != NULL) {
        delete readFile;
        readFile = NULL;
    }

    tcpSocket->close();

    disconnected();
}

void P2PClient::connectedToServer()
{
    connected();
}

void P2PClient::send(QByteArray &bytes)
{
    tcpSocket->write(bytes);
    tcpSocket->flush();
}

bool P2PClient::sendFile(const QString &filename) {
    if (readFile != NULL)
        return false;

    readFile = new QFile(filename);
    readFile->fileName();
    if (readFile->open(QFile::ReadOnly)) {
        connect(tcpSocket, SIGNAL(bytesWritten(qint64)), this, SLOT(sendNextPartOfFile()));
        sendNextPartOfFile();
        return true;
    } else {
        delete readFile;
        readFile = NULL;
        return false;
    }
}

void P2PClient::sendNextPartOfFile() {
    if (readFile == NULL)
        return;

    char block[SIZE_BLOCK_FOR_SEND_FILE];
    if (!readFile->atEnd()) {
        qint64 in = readFile->read(block, sizeof(block));
        tcpSocket->write(block, in);
        emit onFileProgress(readFile->fileName(), readFile->pos(), readFile->size());
    } else {
        emit onFileSent(readFile->fileName());
        readFile->close();
        readFile = NULL;
        disconnect(tcpSocket, SIGNAL(bytesWritten(qint64)), this, SLOT(sendNextPartOfFile()));
    }
}

void P2PClient::read()
{
    if (in == 0) {
        in = new QTextStream(tcpSocket);
        //in->setCodec("UTF-8");
    }

    while (!in->atEnd()) {
        QString line = in->readLine();
        if (line.isEmpty())
            break;
        onDataRecieved(line);
    }
}

void P2PClient::connectionClosedByServer()
{
    disconnectFromServer();
}

void P2PClient::error()
{
    disconnectFromServer();
}

