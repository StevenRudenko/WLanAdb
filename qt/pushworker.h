#ifndef PUSHWORKER_H
#define PUSHWORKER_H

#include <QElapsedTimer>
#include <QObject>
#include <QTextStream>

#include "io_compatibility.h"

class PushWorker : public QObject
{
    Q_OBJECT
public:
    explicit PushWorker(QObject *parent = 0);
    virtual ~PushWorker();
    
signals:
    
public slots:
    void onFileSendingStarted(const QString& filename);
    void onFileSendingProgress(const QString& filename, qint64 sent, qint64 total);
    void onFileSendingEnded(const QString& filename);
    
private:
    QTextStream qout;
    QElapsedTimer timer;

    const int SCREEN_WIDTH;
};

#endif // PUSHWORKER_H
