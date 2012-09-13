#include "installadapter.h"

InstallAdapter::InstallAdapter(QObject *parent) :
    PushAdapter(parent)
{
}

InstallAdapter::~InstallAdapter()
{
}

void InstallAdapter::onFileSendingEnded(const QString &)
{

}
