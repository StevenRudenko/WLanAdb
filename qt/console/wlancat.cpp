#include "wlancat.h"

#include <data/message.pb.h>
#include <commands.h>

#include "./utils/io_compatibility.h"
#include "./utils/myconfig.h"

#include "./adapter/pushadapter.h"
#include "./adapter/logcatadapter.h"
#include "./adapter/installadapter.h"

using namespace std;
using namespace com::wlancat::data;

namespace {
const QString HELP("help");
const QString VERSION("version");
}

WLanCat::WLanCat(int argc, char *argv[]) :
    QObject(NULL), qout(stdout), adapter(NULL)
{
    if (argc < 2) {
        printHelp();
        exit(0);
        return;
    }

    const QString command = argv[1];
    if (0 == VERSION.compare(command)) {
        printVersion();
        exit(0);
        return;
    } else if (0 == HELP.compare(command)) {
        printHelp();
        exit(0);
        return;
    }

    //TODO: fix this
    for (int i=2; i<argc; ++i) {
        QString arg(argv[i]);
        if (0 == arg.compare("-s")) {
            SILENT = true;
            continue;
        }
    }

    proc = WLanAdb::prepareProcessor(argc, argv);
    if (NULL == proc) {
        qout << tr("Can't create processor for command.") << endl;
        printHelp();
        exit(0);
        return;
    }

    connect(&wlanadb, SIGNAL(onClientSearchCompleted(QList<Client>)), this, SLOT(selectClient(QList<Client>)));
    connect(&wlanadb, SIGNAL(onConnectedToClient(Client)), this, SLOT(onConnectedToClient(Client)));
    connect(&wlanadb, SIGNAL(onDisconnectedFromClient()), this, SLOT(onDisconnectedFromClient()));

    wlanadb.searchClients(BROADCAST_PORT, MAX_CLIENT_SEARCH_TRIES);
}

WLanCat::~WLanCat()
{
    if (proc != NULL) {
        delete proc;
        proc = NULL;
    }

    if (adapter != NULL) {
        delete adapter;
        adapter = NULL;
    }
}

void WLanCat::selectClient(const QList<Client> &clients)
{
    const QString command = QString::fromUtf8(proc->getCommand().command().c_str());
    const bool devicesCommand = 0 == Commands::DEVICES.compare(command);

    int size = clients.size();
    if (0 == size) {
        qout << tr("\rThere is no devices found.") << endl;
        exit(0);
    }

    if (!devicesCommand && 1 == size) {
        const Client client = clients.at(0);
        qout << tr("\rConnecting to %1").arg(client.name().c_str()) << endl;
        wlanadb.connectToClient(client);
        return;
    }

    qout << tr("\rThere are %1 devices found:").arg(size) << endl;
    for (int i=0; i<size; ++i) {
        Client client = clients.at(i);
        const QString& clientName = QString::fromStdString(client.name());
        const QString& clientIp = QString::fromStdString(client.ip());
        qout << tr("%1) %2 - %3 (%4)").arg(QString::number(i+1), clientName, clientIp, QString::number(client.port())) << endl;
    }

    if (devicesCommand) {
        exit(0);
        return;
    }

    qout << tr("Please select device [1..%1]:").arg(QString::number(size)) << endl;

    QTextStream qin(stdin);
    int selection;
    qin >> selection;

    if (selection > 0 && selection <= size) {
        const Client client = clients.at(--selection);
        qout << tr("\rConnecting to %1").arg(client.name().c_str()) << endl;
        wlanadb.connectToClient(client);
    } else {
        selectClient(clients);
    }
}

void WLanCat::onConnectedToClient(const Client& client)
{
    const QString clientName = QString::fromStdString(client.name());
    const QString clientIp = QString::fromStdString(client.ip());

    if (!SILENT) {
        qout << tr("\rConnected to %1 - %2 (%3)").arg(clientName, clientIp, QString::number(client.port())) << endl;
    }

    if (client.use_pin()) {
        qout << tr("Client requests PIN to access its log. Please enter PIN:") << endl;

        QString pin;
        QTextStream qin(stdin);

        io_compatibility::setInputEcho(false);
        pin = qin.readLine();
        io_compatibility::setInputEcho(true);

        proc->setPin(pin);
    }

    QString command = QString::fromUtf8(proc->getCommand().command().c_str());

    if (0 == Commands::PUSH.compare(command) || 0 == Commands::INSTALL.compare(command)) {
        if (0 == Commands::PUSH.compare(command))
            adapter = new PushAdapter();
        else
            adapter = new InstallAdapter();

        connect(proc, SIGNAL(onFileSendingStarted(const QString&)), adapter, SLOT(onFileSendingStarted(const QString&)));
        connect(proc, SIGNAL(onFileSendingProgress(const QString&,qint64,qint64)), adapter, SLOT(onFileSendingProgress(const QString&,qint64,qint64)));
        connect(proc, SIGNAL(onFileSendingEnded(const QString&)), adapter, SLOT(onFileSendingEnded(const QString&)));
    } else if (0 == Commands::LOGCAT.compare(command)) {
        adapter = new LogcatAdapter();
        connect(proc, SIGNAL(onLogLine(const QString&)), adapter, SLOT(onLogLine(const QString&)));
    }

    if (!wlanadb.start(proc)) {
        qout << tr("Something went wrong! Please check options and start again.") << endl;
        exit(0);
    }
}

void WLanCat::onDisconnectedFromClient()
{
    if (!SILENT) {
        qout << endl;// << tr("Connection with client was closed") << endl;
    }
    exit(0);
}

void WLanCat::printHelp()
{
    printVersion();
    /*
     -s <clien id>                 - directs command to the with the given client id.
     devices                       - list all connected devices

    device commands:
      adb push <local> <remote>    - copy file/dir to device
      adb pull <remote> [<local>]  - copy file/dir from device
      adb logcat [ <filter-spec> ] - view device log
      adb install [-l] [-r] [-s] [--algo <algorithm name> --key <hex-encoded key> --iv <hex-encoded iv>] <file>
                                   - push this package file to the device and install it
                                     ('-l' means forward-lock the app)
                                     ('-r' means reinstall the app, keeping its data)
                                     ('-s' means install on SD card instead of internal storage)
                                     ('--algo', '--key', and '--iv' mean the file is encrypted already)
      adb uninstall [-k] <package> - remove this app package from the device
                                     ('-k' means keep the data and cache directories)
      adb help                     - show this help message
      adb version                  - show version num
*/
}

void WLanCat::printVersion()
{
    qout << tr("Android Debug Bridge version %1").arg("0.1b") << endl;
}
