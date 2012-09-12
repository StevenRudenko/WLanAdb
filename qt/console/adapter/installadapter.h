#ifndef INSTALLWORKER_H
#define INSTALLWORKER_H

#include "pushadapter.h"

class InstallAdapter : public PushAdapter
{
    Q_OBJECT
public:
    explicit InstallAdapter(QObject *parent = 0);
    virtual ~InstallAdapter();

signals:

public slots:
    virtual void onFileSendingEnded(const QString& filename);
};

#endif // INSTALLWORKER_H
