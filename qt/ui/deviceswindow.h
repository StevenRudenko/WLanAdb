#ifndef DEVICESWINDOW_H
#define DEVICESWINDOW_H

#include <QMainWindow>
#include <QTableWidgetItem>

#include <wlanadb.h>
#include <processor/adbprocessor.h>
#include <data/client.pb.h>

using namespace com::wlanadb::data;

namespace Ui {
class DevicesWindow;
}

class DevicesWindow : public QMainWindow
{
    Q_OBJECT
    
public:
    explicit DevicesWindow(int argc, char *argv[]);
    ~DevicesWindow();

public slots:

    void onClientSelected(QTableWidgetItem* item);

private slots:
    void onConnectedToClient(const Client &client);
    void onDisconnectedFromClient();

    void selectClient(QList<Client> const &clients);

private:
    Ui::DevicesWindow *ui;

    WLanAdb wlanadb;
    AdbProcessor* proc;
    QList<Client> clients;
};

#endif // DEVICESWINDOW_H
