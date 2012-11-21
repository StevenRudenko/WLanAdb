#include "wlanadbterminal.h"

#include <commands.h>

#include "utils/io_compatibility.h"
#include "utils/myconfig.h"

#include "adapter/pushadapter.h"
#include "adapter/logcatadapter.h"
#include "adapter/installadapter.h"

using namespace std;

namespace {
const QString HELP("help");
const QString VERSION("version");
const QString PARAM_SERIAL_NUMBER("-s");
const QString PARAM_CLEAR_STYLE("--no-style");
const QString PARAM_SILENT_MODE("--silent");

int shiftArray(int argc, char* argv[], int pos) {
    for (int i=pos; i<argc; ++i) {
        argv[i] = argv[i+1];
    }
    return --argc;
}

}

WLanAdbTerminal::WLanAdbTerminal(int argc, char *argv[]) :
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

    // looking for params
    QString clientSerialNumber;
    int newArgc = argc;
    for (int i=1; i<newArgc; ++i) {
        QString arg(argv[i]);
        if (arg.startsWith(PARAM_SERIAL_NUMBER)) {
            newArgc = shiftArray(newArgc, argv, i);
            --i;

            clientSerialNumber = arg.remove(0, PARAM_SERIAL_NUMBER.length());
            if (clientSerialNumber.isEmpty()) {
                if (i == newArgc-1) {
                    qout << tr("Fail to parse %1 parameter!").arg(PARAM_SERIAL_NUMBER) << endl;
                    exit(0);
                    return;
                }
                clientSerialNumber = QString(argv[i+1]);

                newArgc = shiftArray(newArgc, argv, i+1);
            }
            qout << tr("Looking for device with serial number: %1").arg(clientSerialNumber) << endl;
        } else if (0 == arg.compare(PARAM_CLEAR_STYLE)) {
            MyConfig::CLEAR_STYLE = true;
            newArgc = shiftArray(newArgc, argv, i);
            --i;
        } else if (0 == arg.compare(PARAM_SILENT_MODE)) {
            MyConfig::SILENT_MODE = true;
            newArgc = shiftArray(newArgc, argv, i);
            --i;
        }
    }

    proc = WLanAdb::prepareProcessor(newArgc, argv);
    if (NULL == proc) {
        qout << tr("Can't create processor for command.") << endl;
        printHelp();
        exit(0);
        return;
    }

    connect(&wlanadb, SIGNAL(onClientSearchCompleted(QList<Client>)), this, SLOT(selectClient(QList<Client>)));
    connect(&wlanadb, SIGNAL(onConnectedToClient(Client)), this, SLOT(onConnectedToClient(Client)));
    connect(&wlanadb, SIGNAL(onDisconnectedFromClient()), this, SLOT(onDisconnectedFromClient()));

    wlanadb.searchClients(MyConfig::BROADCAST_PORT, MyConfig::MAX_CLIENT_SEARCH_TRIES, clientSerialNumber);
}

WLanAdbTerminal::~WLanAdbTerminal()
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

void WLanAdbTerminal::selectClient(const QList<Client> &clients)
{
    const QString command = QString::fromUtf8(proc->getCommand().command().c_str());
    const bool devicesCommand = 0 == Commands::DEVICES.compare(command);

    int size = clients.size();
    if (0 == size) {
        qout << tr("\rThere is no any device found.") << endl;
        exit(0);
    }

    if (!devicesCommand && 1 == size) {
        const Client client = clients.at(0);
        qout << tr("\rConnecting to %1").arg(client.name().c_str()) << endl;
        wlanadb.connectToClient(client);
        return;
    }

    qout << tr("\r%1 device(s) found:").arg(size) << endl;
    for (int i=0; i<size; ++i) {
        Client client = clients.at(i);
        const QString& clientId = QString::fromStdString(client.id());
        const QString& clientName = QString::fromStdString(client.name());
        const QString& clientModel = QString::fromStdString(client.model());
        const QString& clientFirmware = QString::fromStdString(client.firmware());
        qout << tr("%1) %2\t%3 - %4 (%5)").arg(QString::number(i+1), clientId, clientName, clientModel, clientFirmware) << endl;
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

void WLanAdbTerminal::onConnectedToClient(const Client& client)
{
    qout << tr("\rConnected to %1").arg(client.name().c_str()) << endl;

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

void WLanAdbTerminal::onDisconnectedFromClient()
{
    qout << endl;
    exit(0);
}

void WLanAdbTerminal::printHelp()
{
    printVersion();
    qout << tr("Usage: WLanAdbTerminal [--no-style] [--silent] [-s] <command> [command params]") << endl;
    qout << tr("-s <serial number>            - directs command to the with the given serial number") << endl;
    qout << tr("--no-style                    - prevent formating output for some commands") << endl;
    qout << tr("--silent                      - prevent output of some texts (not implemented yet)") << endl;
    qout << endl;
    qout << tr("Commands:") << endl;
    qout << tr("  devices                     - list all devices online") << endl;
    qout << tr("  logcat [ <filter-spec> ]    - view device log") << endl;
    qout << tr("                                    '--app=<package name>' show logs for specified package only") << endl;
    qout << tr("                                       Note: can be used multiple times.") << endl;
    qout << tr("                                    '--pid=<proces id>' show logs for selected PID.") << endl;
    qout << tr("                                       Note: can be used multiple times.") << endl;
    qout << tr("                                    '--type=[VDIWE]' show logs of selected types only") << endl;
    qout << endl;
    qout << tr("----------------------------------------------------------------------------------------------------") << endl;
    qout << tr("  WARNING! Starting from Jelly Bean (SDK 16) release every application can read its own logs only!") << endl;
    qout << tr("           Don't worry. You can grand permission to read all logs for WLanAdb by next command:") << endl;
    qout << endl;
    qout << tr("           adb shell pm grant com.wlanadb android.permission.READ_LOGS") << endl;
    qout << tr("----------------------------------------------------------------------------------------------------") << endl;
    qout << endl;
    qout << tr("  push <file>                 - copy file to device") << endl;
    qout << tr("  install <file> [-l]         - push this package file to the device and install it") << endl;
    qout << tr("                                    '-l' means auto-launch app after install") << endl;
    qout << tr("  help                        - show this help message") << endl;
    qout << tr("  version                     - show version number") << endl;
    qout << endl;
    qout << tr("Examples:") << endl;
    qout << tr("     WLanAdbTerminal devices") << endl;
    qout << tr("     WLanAdbTerminal push ~/file_to_push.txt") << endl;
    qout << tr("     WLanAdbTerminal logcat --app='com.wlanadb' --type='VD'") << endl;
}

void WLanAdbTerminal::printVersion()
{
    qout << tr("Wireless Android Debug Bridge version %1").arg("0.1b") << endl;
}
