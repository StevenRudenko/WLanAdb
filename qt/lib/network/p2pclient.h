#ifndef P2PCLIENT_H
#define P2PCLIENT_H

#include <QFile>
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

    void onFileSendingStarted(const QString& filename);
    void onFileSendingProgress(const QString& filename, qint64 sent, qint64 total);
    void onFileSendingEnded(const QString& filename);

public slots:

    void connectToServer(const QString& server, int port);
    void disconnectFromServer();

    void send(QByteArray& bytes);
    bool sendFile(const QString& filename);

private slots:
    void sendNextPartOfFile();
    void read();
    void connectionClosedByServer();
    void error();

private:
    QTcpSocket* tcpSocket;
    QFile* readFile;

    QString notFinishedLine;
};

#endif // P2PCLIENT_H
