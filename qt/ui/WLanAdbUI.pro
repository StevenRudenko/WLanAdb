#-------------------------------------------------
#
# Project created by QtCreator 2012-09-13T15:54:11
#
#-------------------------------------------------

QT       += core gui network

TARGET = WLanAdbUI
TEMPLATE = app


SOURCES += main.cpp\
        deviceswindow.cpp

HEADERS  += deviceswindow.h \
    utils/myconfig.h

FORMS    += deviceswindow.ui

INCLUDEPATH += $$PWD/../lib/
DEPENDPATH += $$PWD/../lib/

# WLanAdbLib library
win32: LIBS += -L$$PWD/../WLanAdbLib-bin/release/ -lWLanAdbLib
else:unix: LIBS += -L$$PWD/../WLanAdbLib-bin/ -lWLanAdbLib

# Protobuf library
win32: LIBS += -L$$PWD/../libs/ -lprotobuf-lite
else:unix: LIBS += -L/usr/local/lib -lprotobuf-lite
