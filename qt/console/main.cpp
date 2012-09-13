#include <QtCore/QCoreApplication>

#include "wlanadbterminal.h"

int main(int argc, char *argv[])
{
    QCoreApplication a(argc, argv);

    WLanAdbTerminal* wlanadb = new WLanAdbTerminal(argc, argv);

    int result = a.exec();

    delete wlanadb;

    return result;
}
