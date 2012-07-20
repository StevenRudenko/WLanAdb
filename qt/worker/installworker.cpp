#include "installworker.h"

InstallWorker::InstallWorker(QObject *parent) :
    PushWorker(parent)
{
}

InstallWorker::~InstallWorker()
{
}

void InstallWorker::onFileSendingEnded(const QString &filename)
{

}
