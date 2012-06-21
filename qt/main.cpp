#include <QtCore/QCoreApplication>

#include "wlancat.h"

int main(int argc, char *argv[])
{
    QCoreApplication a(argc, argv);

    WLanCat* wlancat = new WLanCat(argc, argv);

    int result = a.exec();

    delete wlancat;

    return result;
}
