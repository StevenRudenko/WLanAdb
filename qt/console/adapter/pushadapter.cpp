#include "pushadapter.h"

#include <QFileInfo>
#include <QTime>

#include "utils/utils.h"
#include "utils/myconfig.h"

namespace {

const int PERCENT_WIDTH = 4;
const int SENT_WIDTH = 10;
const int SPEED_WIDTH = 10;
const int ETA_WIDTH = 12;
const int HEADER_WIDTH = 47;

const double b = 1;
const double Kb = 1024 * b;
const double Mb = 1024 * Kb;
const double Gb = 1024 * Mb;
const double Tb = 1024 * Gb;

QString readableSize(qint64 bytes) {
    int prec = 2;
    QString unit;
    float value;
    if (bytes > Gb) {
        value = bytes / Gb;
        unit = "Gb";
    } else {
        if (bytes > Mb) {
            value = bytes / Mb;
            unit = "Mb";
        } else {
            if (bytes > Kb) {
                value = bytes / Kb;
                unit = "Kb";
                prec = 0;
            } else {
                value = bytes;
                unit = "b";
                prec = 0;
            }
        }
    }
    QString result;
    result.append(QString::number(value, 'f', prec));
    result.append(unit);
    return result;
}

}

PushAdapter::PushAdapter(QObject *parent) :
    Adapter(parent)
{
}

PushAdapter::~PushAdapter()
{
}

void PushAdapter::onFileSendingStarted(const QString &filename)
{
    qout << tr("\rSending %1...").arg(filename) << endl;
    timer.start();
}

void PushAdapter::onFileSendingProgress(const QString &, qint64 sent, qint64 total)
{
    const quint64 elapsed = timer.elapsed();
    if (elapsed == 0)
        return;

    const quint64 speed = 1000 * sent / elapsed;
    const int eta_time = (total - sent) / speed;

    const int progress_area = SCREEN_WIDTH - HEADER_WIDTH;

    const double percent = (double)sent / (double)total;
    const int progress = 100.0 * percent;
    const int done = percent * progress_area;
    const int notdone = progress_area - done;

    QString text_done(done, '=');
    text_done.append(">");
    QString text_notdone(notdone, ' ');

    QString text_percent;
    text_percent.setNum(progress);
    text_percent = text_percent.rightJustified(2, '0');
    text_percent.append('%');
    text_percent = text_percent.leftJustified(PERCENT_WIDTH, ' ');

    QString text_sent = readableSize(sent);
    text_sent = text_sent.leftJustified(SENT_WIDTH, ' ');

    QString text_speed = readableSize(speed);
    text_speed.append("/s");
    text_speed = text_speed.leftJustified(SPEED_WIDTH, ' ');

    QTime time;
    time = time.addSecs(eta_time);
    QString text_eta = time.toString(Qt::TextDate);
    text_eta = text_eta.leftJustified(ETA_WIDTH, ' ');

    qout << "\r" << text_percent << "[" << text_done << text_notdone << "] " << text_sent << " " << text_speed << " eta " << text_eta;
    qout.flush();
}

void PushAdapter::onFileSendingEnded(const QString &filename)
{
    quint64 elapsed = timer.elapsed() / 1000;
    QTime time;
    time = time.addSecs(elapsed);
    qout << endl << tr("%1 was sent within %2").arg(filename, time.toString(Qt::TextDate)) << endl;
}
