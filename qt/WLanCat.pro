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
    message.pb.cc \
    client.pb.cc \
    broadcastserver.cpp \
    wlancat.cpp \
    p2pclient.cpp \
    command.pb.cc \
    logwriter.cpp \
    io_compatibility.cpp

HEADERS += \
    message.pb.h \
    client.pb.h \
    broadcastserver.h \
    wlancat.h \
    p2pclient.h \
    command.pb.h \
    logwriter.h \
    io_compatibility.h

TRANSLATIONS = main_en.ts \
               main_ru.ts \
               main_ua.ts

win32: LIBS += -L$$PWD/libs/ -lprotobuf
else:unix: LIBS += -L/usr/local/lib -lprotobuf

INCLUDEPATH += $$PWD/protobuf-include
DEPENDPATH += $$PWD/protobuf-include

win32: PRE_TARGETDEPS += $$PWD/libs/libprotobuf.dll.a
else:unix: PRE_TARGETDEPS += /usr/local/lib/libprotobuf.a
