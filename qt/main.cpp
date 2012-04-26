#include <QtCore/QCoreApplication>
#include <QTextStream>

using namespace std;

int main(int argc, char *argv[])
{
    QCoreApplication a(argc, argv);

    QTextStream qout(stdout);

    qout << "Hello World!" << endl;

    return a.exec();
}
