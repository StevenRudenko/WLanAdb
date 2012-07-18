#ifndef WORKER_H
#define WORKER_H

#include <QObject>
#include <QTextStream>

#include "command.pb.h"

using namespace com::wlancat::data;

class Worker : public QObject
{
    Q_OBJECT
public:
    explicit Worker(QObject *parent = 0);
    virtual ~Worker();
    
public:
    virtual Command getCommand(Command& command) = 0;

signals:
    
public slots:
    
protected:
    const int SCREEN_WIDTH;
    QTextStream qout;
};

#endif // WORKER_H
