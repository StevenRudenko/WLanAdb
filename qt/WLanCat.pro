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

LIBS += -L/usr/local/lib -lprotobuf

SOURCES += main.cpp \
    message.pb.cc \
    client.pb.cc \
    broadcastserver.cpp \
    wlancat.cpp \
    p2pclient.cpp \
    command.pb.cc

HEADERS += \
    message.pb.h \
    client.pb.h \
    broadcastserver.h \
    wlancat.h \
    p2pclient.h \
    command.pb.h

TRANSLATIONS = main_en.ts \
               main_ru.ts \
               main_ua.ts
