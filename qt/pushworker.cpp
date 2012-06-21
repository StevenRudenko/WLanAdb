#include "pushworker.h"

namespace {

const int PERCENT_WIDTH = 4;
const int HEADER_WIDTH = 38;

}

PushWorker::PushWorker(QObject *parent) :
    QObject(parent), qout(stdout), SCREEN_WIDTH(io_compatibility::getConsoleWidth())
{

}

PushWorker::~PushWorker() {

}

void PushWorker::onFileSent(const QString &filename)
{
    //qout << "\r" << filename << " sent" << endl;
    exit(0);
}

void PushWorker::onFileProgress(const QString &filename, int sent, int total)
{
    //<4->[           ]<------ 34 ---------------------->
    //39% [=====>     ] 1,728,245    104K/s  eta 31s
    //2012-06-21 17:39:55 (94.9 KB/s) - `TheAnnoyingOrange.mp4' saved [4348238/4348238]
    const int progress_area = SCREEN_WIDTH - HEADER_WIDTH;
    const float percent = (float)sent / (float)total;
    const int progress = 100.f * percent;
    const int done = percent * progress_area;
    const int notdone = progress_area - done;
    QString text_done(done - 1, '=');
    QString text_notdone(notdone, ' ');
    QString text_percent;
    text_percent.setNum(progress);
    text_percent = text_percent.rightJustified(2, '0');
    text_percent.append('%');
    text_percent = text_percent.leftJustified(PERCENT_WIDTH, ' ');
    qout << "\r" << text_percent << "[" << text_done << '>' << text_notdone << "]";
    qout.flush();
}
