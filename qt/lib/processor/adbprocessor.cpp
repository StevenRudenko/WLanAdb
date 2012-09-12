#include "adbprocessor.h"

#include "utils/utils.h"

AdbProcessor::AdbProcessor(Command * cmd) :
    QObject(NULL), cmd(cmd)
{
}

AdbProcessor::~AdbProcessor()
{
    if (cmd != NULL) {
        delete cmd;
        cmd = NULL;
    }
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
