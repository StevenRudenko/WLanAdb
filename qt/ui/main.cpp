#include <QtGui/QApplication>
#include "deviceswindow.h"

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    DevicesWindow w;
    w.setArgs(argc, argv);
    w.show();
    
    return a.exec();
}
