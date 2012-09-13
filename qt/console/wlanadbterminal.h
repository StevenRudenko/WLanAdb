#ifndef WLanAdbTerminal_H
#define WLanAdbTerminal_H

#include <QObject>
#include <QString>
#include <QTextStream>

#include <wlanadb.h>
#include <processor/adbprocessor.h>

#include <data/client.pb.h>

#include "./adapter/adapter.h"

using namespace com::wlanadb::data;

class WLanAdbTerminal : public QObject
{
    Q_OBJECT
public:
    WLanAdbTerminal(int argc, char *argv[]);
    virtual ~WLanAdbTerminal();

private slots:

    void onConnectedToClient(const Client &client);
    void onDisconnectedFromClient();

    void selectClient(QList<Client> const &clients);

private:

    void printHelp();
    void printVersion();

private:
    QTextStream qout;

    Adapter* adapter;

    WLanAdb wlanadb;
    AdbProcessor* proc;
};

#endif // WLanAdbTerminal_H
