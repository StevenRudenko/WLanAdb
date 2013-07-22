#include "deviceswindow.h"
#include "ui_deviceswindow.h"

#include "utils/myconfig.h"

#include <commands.h>

#include <QInputDialog>
#include <QFileDialog>
#include <QListView>
#include <QTreeView>

#include "utils/utils.h"

DevicesWindow::DevicesWindow(int argc, char *argv[]) :
    QMainWindow(NULL),
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

    proc = WLanAdb::prepareProcessor(argc, argv);

    wlanadb.searchClients(BROADCAST_PORT, MAX_CLIENT_SEARCH_TRIES);

    ui->statusBar->showMessage(tr("Searching for clients..."));

    show();
}

DevicesWindow::~DevicesWindow()
{
    delete ui;

    if (proc != NULL) {
        delete proc;
        proc = NULL;
    }
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

    if (0 == size) {
        ui->statusBar->showMessage(tr("No clients found"));
        //    } else if (1 == size) {
        //        onClientSelected(ui->devicesTableWidget->item(0, 1));
    } else {
        ui->statusBar->showMessage(tr("Select client"));
    }
}

void DevicesWindow::onConnectedToClient(const Client& client)
{
    if (client.use_pin()) {
        ui->statusBar->showMessage(tr("PIN code required"));

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

    ui->statusBar->showMessage(tr("Connected to client"));

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
        QFileDialog w;
        w.setFileMode(QFileDialog::AnyFile);
        w.setOption(QFileDialog::DontUseNativeDialog,true);
        QListView *l = w.findChild<QListView*>("listView");
        if (l) {
            l->setSelectionMode(QAbstractItemView::MultiSelection);
        }
        QTreeView *t = w.findChild<QTreeView*>();
        if (t) {
            t->setSelectionMode(QAbstractItemView::MultiSelection);
        }
        int nMode = w.exec();
        QStringList result = w.selectedFiles();
        foreach (const QString &str, result) {
            qDebug(str.toStdString().c_str());
        }

        //exit(0);
        return;
    }

    //ui->statusBar->showMessage(tr("Connecting to client..."));

    const int row = item->row();
    const Client client = clients.at(row);
    wlanadb.connectToClient(client);
}
