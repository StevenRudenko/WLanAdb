#ifndef INSTALLPROCESSOR_H
#define INSTALLPROCESSOR_H

#include "push.h"

class Install : public Push
{
    Q_OBJECT

public:
    Install(Command* cmd = NULL);
    virtual ~Install();

signals:
    void onFileSendingStarted(const QString& filename);
    void onFileSendingProgress(const QString& filename, qint64 sent, qint64 total);
    void onFileSendingEnded(const QString& filename);

};

#endif // INSTALLPROCESSOR_H
