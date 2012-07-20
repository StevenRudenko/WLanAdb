#-------------------------------------------------
#
# Project created by QtCreator 2012-04-26T23:02:04
#
#-------------------------------------------------

QT       += core, network
QT       -= gui

TARGET = WLanCat
CONFIG   += console
CONFIG   -= app_bundle

TEMPLATE = app

SOURCES += main.cpp \
    broadcastserver.cpp \
    wlancat.cpp \
    p2pclient.cpp \
    io_compatibility.cpp \
    utils.cpp \
    worker/installworker.cpp \
    worker/worker.cpp \
    worker/pushworker.cpp \
    worker/logcatworker.cpp \
    data/message.pb.cc \
    data/command.pb.cc \
    data/client.pb.cc

HEADERS += \
    broadcastserver.h \
    wlancat.h \
    p2pclient.h \
    io_compatibility.h \
    utils.h \
    worker/installworker.h \
    worker/worker.h \
    worker/pushworker.h \
    worker/logcatworker.h \
    data/message.pb.h \
    data/command.pb.h \
    data/client.pb.h

TRANSLATIONS = main_en.ts \
               main_ru.ts \
               main_ua.ts

win32: LIBS += -L$$PWD/libs/ -lprotobuf
else:unix: LIBS += -L/usr/local/lib -lprotobuf

INCLUDEPATH += $$PWD/protobuf-include
DEPENDPATH += $$PWD/protobuf-include

win32: PRE_TARGETDEPS += $$PWD/libs/libprotobuf.dll.a
else:unix: PRE_TARGETDEPS += /usr/local/lib/libprotobuf.a
