#ifndef P2PCLIENT_H
#define P2PCLIENT_H

#include <QObject>
#include <QString>
#include <QTcpSocket>

class P2PClient : public QObject
{
    Q_OBJECT
public:
    explicit P2PClient(QObject *parent = 0);
    virtual ~P2PClient();

signals:

    void connected();
    void disconnected();
    void onDataRecieved(const QString& data);

public slots:

    void connectToServer(const QString& server, int port);
    void disconnectFromServer();

private slots:
    void connectedToServer();
    void read();
    void connectionClosedByServer();
    void error();

private:
    QTextStream* in;
    QTcpSocket* tcpSocket;
    quint16 nextBlockSize;
};

#endif // P2PCLIENT_H
