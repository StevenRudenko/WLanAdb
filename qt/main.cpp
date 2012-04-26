#include <QtCore/QCoreApplication>
#include <QTextStream>

#include "message.pb.h"

using namespace std;
using namespace com::wlancat::data;

int main(int argc, char *argv[])
{
    // Verify that the version of the library that we linked against is
    // compatible with the version of the headers we compiled against.
    GOOGLE_PROTOBUF_VERIFY_VERSION;

    QCoreApplication a(argc, argv);

    QTextStream qout(stdout);

    qout << "Hello World!" << endl;

    Message* msg = new Message();
    msg->set_type(Message_Type_REQEST);

    QString qstr = QString::fromStdString(msg->DebugString());
    qout << qstr << endl;

    delete msg;

    int result = a.exec();

    // Optional:  Delete all global objects allocated by libprotobuf.
    google::protobuf::ShutdownProtobufLibrary();

    return result;
}
