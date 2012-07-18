#ifndef LOGWRITER_H
#define LOGWRITER_H

#include <QHash>
#include <QObject>
#include <QRegExp>
#include <QString>
#include <QTextStream>

#include "worker.h"

class LogcatWorker : public Worker
{
    Q_OBJECT
public:
    explicit LogcatWorker(QObject *parent = 0);
    virtual ~LogcatWorker();
    
public:
    virtual Command getCommand(Command &command);

signals:
    
public slots:
    void onLogLine(const QString& str);

private:
    void printProcess(const QString& processString);
    void printTagType(const QString& typeString);
    void printTag(const QString& tagString);
    void printMessage(const QString& messageString);

private:
    QTextStream qin;

    QRegExp logRegEx;
    QHash<QString, int> tagsList;
    int nextColor;
};

#endif // LOGWRITER_H
