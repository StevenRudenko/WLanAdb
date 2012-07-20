#ifndef INSTALLWORKER_H
#define INSTALLWORKER_H

#include "pushworker.h"

class InstallWorker : public PushWorker
{
    Q_OBJECT
public:
    explicit InstallWorker(QObject *parent = 0);
    virtual ~InstallWorker();

signals:

public slots:
    virtual void onFileSendingEnded(const QString& filename);
};

#endif // INSTALLWORKER_H
