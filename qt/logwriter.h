#ifndef LOGWRITER_H
#define LOGWRITER_H

#include <QHash>
#include <QObject>
#include <QRegExp>
#include <QString>
#include <QTextStream>

#include "io_compatibility.h"

class LogWriter : public QObject
{
    Q_OBJECT
public:
    explicit LogWriter(QObject *parent = 0);
    virtual ~LogWriter();
    
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
    QTextStream qout;

    QRegExp logRegEx;
    QHash<QString, int> tagsList;
    int nextColor;

    const int SCREEN_WIDTH;
};

#endif // LOGWRITER_H
