#ifndef UTILS_H
#define UTILS_H

#include <QFile>

namespace utils {

QString getFileChecksum(const QString& filename);

QString getHash(const QString& text);

}

#endif // UTILS_H
