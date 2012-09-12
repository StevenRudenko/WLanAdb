#-------------------------------------------------
#
# Project created by QtCreator 2012-08-01T19:34:56
#
#-------------------------------------------------

QT       += network
QT       -= gui

TARGET = WLanAdbLib
TEMPLATE = lib
CONFIG += staticlib

SOURCES += \
    data/message.pb.cc \
    data/command.pb.cc \
    data/client.pb.cc \
    network/p2pclient.cpp \
    network/broadcastserver.cpp \
    processor/devices.cpp \
    processor/logcat.cpp \
    processor/adbprocessor.cpp \
    wlanadb.cpp \
    processor/install.cpp \
    processor/push.cpp \
    utils/utils.cpp

HEADERS += \
    data/message.pb.h \
    data/command.pb.h \
    data/client.pb.h \
    network/p2pclient.h \
    network/broadcastserver.h \
    processor/logcat.h \
    processor/devices.h \
    processor/adbprocessor.h \
    wlanadb.h \
    processor/install.h \
    processor/push.h \
    commands.h \
    utils/utils.h

INCLUDEPATH += $$PWD/protobuf-include
DEPENDPATH += $$PWD/protobuf-include

win32: LIBS += -L$$PWD/../libs/ -lprotobuf-lite
else:unix: LIBS += -L/usr/local/lib -lprotobuf-lite
