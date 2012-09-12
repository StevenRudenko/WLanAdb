#ifndef WLANCAT_H
#define WLANCAT_H

#include <QObject>
#include <QString>
#include <QTextStream>

#include <wlanadb.h>
#include <processor/adbprocessor.h>

#include <data/client.pb.h>

#include "./adapter/adapter.h"

using namespace com::wlancat::data;

class WLanCat : public QObject
{
    Q_OBJECT
public:
    WLanCat(int argc, char *argv[]);
    virtual ~WLanCat();

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

#endif // WLANCAT_H
