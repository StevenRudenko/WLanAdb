#ifndef PUSHWORKER_H
#define PUSHWORKER_H

#include <QElapsedTimer>
#include <QObject>
#include <QString>
#include <QTextStream>

#include "worker.h"

using namespace com::wlancat::data;

class PushWorker : public Worker
{
    Q_OBJECT
public:
    explicit PushWorker(QObject *parent = 0);
    virtual ~PushWorker();
    
public:
    virtual Command getCommand(Command &command);
    bool setFilename(const QString& filename);

signals:
    
public slots:
    void onFileSendingStarted(const QString& filename);
    void onFileSendingProgress(const QString& filename, qint64 sent, qint64 total);
    void onFileSendingEnded(const QString& filename);
    
private:
    QString filename;
    QElapsedTimer timer;
};

#endif // PUSHWORKER_H
