#ifndef LOGWRITER_H
#define LOGWRITER_H

#include <QHash>
#include <QObject>
#include <QRegExp>
#include <QString>
#include <QTextStream>

#include "adapter.h"

class LogcatAdapter : public Adapter
{
    Q_OBJECT
public:
    explicit LogcatAdapter(QObject *parent = 0);
    virtual ~LogcatAdapter();
    
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
