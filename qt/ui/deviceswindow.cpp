#include "deviceswindow.h"
#include "ui_deviceswindow.h"

#include "utils/myconfig.h"

#include <commands.h>

#include <QInputDialog>

#include "utils/utils.h"

DevicesWindow::DevicesWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::DevicesWindow)
{
    setWindowFlags( (windowFlags() | Qt::CustomizeWindowHint) & ~Qt::WindowMaximizeButtonHint);
    setFixedSize(521, 300);

    ui->setupUi(this);
    ui->devicesTableWidget->setColumnWidth(0, 21);
    ui->devicesTableWidget->setColumnWidth(1, 150);

    connect(ui->devicesTableWidget, SIGNAL(itemDoubleClicked(QTableWidgetItem*)), this, SLOT(onClientSelected(QTableWidgetItem*)));

    connect(&wlanadb, SIGNAL(onClientSearchCompleted(QList<Client>)), this, SLOT(selectClient(QList<Client>)));
    connect(&wlanadb, SIGNAL(onConnectedToClient(Client)), this, SLOT(onConnectedToClient(Client)));
    connect(&wlanadb, SIGNAL(onDisconnectedFromClient()), this, SLOT(onDisconnectedFromClient()));

    wlanadb.searchClients(BROADCAST_PORT, MAX_CLIENT_SEARCH_TRIES);
}

DevicesWindow::~DevicesWindow()
{
    delete ui;

    if (proc != NULL) {
        delete proc;
        proc = NULL;
    }
}

void DevicesWindow::setArgs(int argc, char *argv[])
{
    proc = WLanAdb::prepareProcessor(argc, argv);
}

void DevicesWindow::selectClient(const QList<Client> &clients)
{
    this->clients = clients;
    int size = clients.size();
    ui->devicesTableWidget->setRowCount(size);
    for (int i=0; i<size; ++i) {
        const Client client = clients.at(i);
        const QString& clientId = QString::fromStdString(client.id());
        const QString& clientName = QString::fromStdString(client.name());
        const QString& clientModel = QString::fromStdString(client.model());
        const QString& clientFirmware = QString::fromStdString(client.firmware());


        if (client.use_pin()) {
            QIcon lock(":/images/icon_lock.png");
            QTableWidgetItem *pin = new QTableWidgetItem;
            pin->setIcon(lock);
            ui->devicesTableWidget->setItem(i, 0, pin);
        }
        QTableWidgetItem *id = new QTableWidgetItem(clientId);
        QTableWidgetItem *name = new QTableWidgetItem(clientName);
        QTableWidgetItem *model = new QTableWidgetItem(clientModel);
        QTableWidgetItem *firmware = new QTableWidgetItem(clientFirmware);
        ui->devicesTableWidget->setItem(i, 1, id);
        ui->devicesTableWidget->setItem(i, 2, name);
        ui->devicesTableWidget->setItem(i, 3, model);
        ui->devicesTableWidget->setItem(i, 4, firmware);
    }
}

void DevicesWindow::onConnectedToClient(const Client& client)
{
    if (client.use_pin()) {
        bool ok;
        QString pin = QInputDialog::getText(this, tr("Enter PIN"),
                                            tr("PIN code:"), QLineEdit::Password, QString(), &ok);
        if (!ok || pin.isEmpty()) {
            exit(0);
            return;
        }

        proc->setPin(pin.toUtf8());
    }

    QString command = QString::fromUtf8(proc->getCommand().command().c_str());

    if (0 == Commands::PUSH.compare(command) || 0 == Commands::INSTALL.compare(command)) {
        // just waiting for file transfaring finished
    } else if (0 == Commands::LOGCAT.compare(command)) {
        // ignoring this request as far we don't have UI to show LogCat
        exit(0);
        return;
    }

    if (!wlanadb.start(proc)) {
        exit(0);
        return;
    }

    hide();
}

void DevicesWindow::onDisconnectedFromClient()
{
    exit(0);
}

void DevicesWindow::onClientSelected(QTableWidgetItem *item)
{
    if (NULL == proc) {
        exit(0);
        return;
    }

    const int row = item->row();
    const Client client = clients.at(row);
    wlanadb.connectToClient(client);
}
