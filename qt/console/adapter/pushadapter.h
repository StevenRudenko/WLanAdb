#ifndef PUSHWORKER_H
#define PUSHWORKER_H

#include <QElapsedTimer>
#include <QObject>
#include <QString>
#include <QTextStream>

#include "adapter.h"

using namespace com::wlanadb::data;

class PushAdapter : public Adapter
{
    Q_OBJECT
public:
    explicit PushAdapter(QObject *parent = 0);
    virtual ~PushAdapter();

signals:
    
public slots:
    virtual void onFileSendingStarted(const QString& filename);
    virtual void onFileSendingProgress(const QString& filename, qint64 sent, qint64 total);
    virtual void onFileSendingEnded(const QString& filename);
    
private:
    QString filename;
    QElapsedTimer timer;
};

#endif // PUSHWORKER_H
