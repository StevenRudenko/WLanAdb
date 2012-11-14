#ifndef WORKER_H
#define WORKER_H

#include <QObject>
#include <QTextStream>

#include "data/command.pb.h"

using namespace com::wlanadb::data;

class Adapter : public QObject
{
    Q_OBJECT
public:
    explicit Adapter(QObject *parent = 0);
    virtual ~Adapter();

signals:
    
public slots:
    
protected:
    const int SCREEN_WIDTH;
    QTextStream qout;
};

#endif // WORKER_H
