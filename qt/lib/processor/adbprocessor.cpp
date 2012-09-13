#include "adbprocessor.h"

#include "utils/utils.h"

AdbProcessor::AdbProcessor(Command * cmd) :
    QObject(NULL), cmd(cmd)
{
    // Verify that the version of the library that we linked against is
    // compatible with the version of the headers we compiled against.
    GOOGLE_PROTOBUF_VERIFY_VERSION;
}

AdbProcessor::~AdbProcessor()
{
    if (cmd != NULL) {
        delete cmd;
        cmd = NULL;
    }

    // Optional:  Delete all global objects allocated by libprotobuf.
    google::protobuf::ShutdownProtobufLibrary();
}

Command &AdbProcessor::getCommand() const
{
    return *cmd;
}

bool AdbProcessor::start(P2PClient *p2pClient)
{
    QByteArray data;
    QDataStream request(&data, QIODevice::WriteOnly);
    request << cmd->ByteSize();
    request.writeRawData(cmd->SerializeAsString().c_str(), cmd->ByteSize());
    p2pClient->send(data);

    return true;
}

void AdbProcessor::setPin(const QString &pin) {
    // encoding pin
    const QString hashedPin = utils::getHash(pin);
    cmd->set_pin(hashedPin.toStdString());
}
