#include <QtGui/QApplication>
#include "deviceswindow.h"

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    DevicesWindow w(argc, argv);
    
    return a.exec();
}
