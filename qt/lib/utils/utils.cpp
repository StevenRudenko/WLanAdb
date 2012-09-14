#include "utils.h"

#include <QCryptographicHash>

namespace utils {

QString getFileChecksum(const QString& filename) {
    QCryptographicHash checksum(QCryptographicHash::Md5);
    QFile file(filename);
    file.open(QFile::ReadOnly);
    while(!file.atEnd()) {
        checksum.addData(file.read(8192));
    }
    return checksum.result().toHex();
}

QString getHash(const QString& text) {
    QByteArray data;
    data.append(text);
    return QCryptographicHash::hash(data, QCryptographicHash::Sha1).toBase64();
}

}
