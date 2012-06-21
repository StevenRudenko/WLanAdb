#ifndef PUSHWORKER_H
#define PUSHWORKER_H

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
    void onFileSent(const QString& filename);
    void onFileProgress(const QString& filename, int sent, int total);
    
private:
    QTextStream qout;
    const int SCREEN_WIDTH;
};

#endif // PUSHWORKER_H
