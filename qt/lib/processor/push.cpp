#include "push.h"

#include <QFileInfo>

#include "utils/utils.h"

Push::Push(Command *cmd) :
    AdbProcessor(cmd)
{
    QString filename = QString::fromUtf8(cmd->params(0).c_str());
    QFileInfo fileInfo(filename);
    if (!fileInfo.isFile() || !fileInfo.exists())
        return;

    QString checksum = utils::getFileChecksum(filename);
    cmd->set_checksum(checksum.toStdString());
    cmd->set_length(fileInfo.size());
}

Push::~Push() {

}

bool Push::start(P2PClient *p2pClient)
{
    connect(p2pClient, SIGNAL(onFileSendingStarted(const QString&)), this, SIGNAL(onFileSendingStarted(const QString&)));
    connect(p2pClient, SIGNAL(onFileSendingProgress(const QString&,qint64,qint64)), this, SIGNAL(onFileSendingProgress(const QString&,qint64,qint64)));
    connect(p2pClient, SIGNAL(onFileSendingEnded(const QString&)), this, SIGNAL(onFileSendingEnded(const QString&)));

    AdbProcessor::start(p2pClient);

    QString filename = QString::fromUtf8(getCommand().params(0).c_str());
    return p2pClient->sendFile(filename);
}
