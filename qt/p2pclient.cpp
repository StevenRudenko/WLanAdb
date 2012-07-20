#include "p2pclient.h"

namespace {
const int FILE_BUFFER = 32 * 1024;
const int READ_BUFFER = 4 * 1024;
}

P2PClient::P2PClient(QObject *parent) :
    QObject(parent), in(NULL), readFile(NULL)
{

    tcpSocket = new QTcpSocket();
    tcpSocket->setSocketOption(QAbstractSocket::KeepAliveOption, 1);
    tcpSocket->setSocketOption(QAbstractSocket::LowDelayOption, 1);
    tcpSocket->setReadBufferSize(READ_BUFFER);

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
    tcpSocket->abort();
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
    if (readFile->open(QFile::ReadOnly)) {
        connect(tcpSocket, SIGNAL(bytesWritten(qint64)), this, SLOT(sendNextPartOfFile()));

        emit onFileSendingStarted(readFile->fileName());
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

    char block[FILE_BUFFER];
    if (!readFile->atEnd()) {
        qint64 read = readFile->read(block, sizeof(block));
        tcpSocket->write(block, read);
        emit onFileSendingProgress(readFile->fileName(), readFile->pos(), readFile->size());
    } else {
        tcpSocket->flush();
        disconnect(tcpSocket, SIGNAL(bytesWritten(qint64)), this, SLOT(sendNextPartOfFile()));

        emit onFileSendingEnded(readFile->fileName());
        readFile->close();
        delete readFile;
        readFile = NULL;
    }
}

void P2PClient::read()
{
    if (in == 0) {
        in = new QTextStream(tcpSocket);
        //in->setCodec("UTF-8");
    }

    while (!in->atEnd()) {
        if (!tcpSocket->canReadLine()) {
            in->readLine();
            break;
        }

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

