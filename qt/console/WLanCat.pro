#-------------------------------------------------
#
# Project created by QtCreator 2012-04-26T23:02:04
#
#-------------------------------------------------

QT       += core, network
QT       -= gui

TARGET = WLanCat
CONFIG   += console
CONFIG   += static
CONFIG   -= app_bundle

TEMPLATE = app

SOURCES += main.cpp \
    wlancat.cpp \
    adapter/adapter.cpp \
    adapter/installadapter.cpp \
    adapter/logcatadapter.cpp \
    adapter/pushadapter.cpp \
    utils/io_compatibility.cpp

HEADERS += \
    wlancat.h \
    adapter/adapter.h \
    adapter/installadapter.h \
    adapter/logcatadapter.h \
    adapter/pushadapter.h \
    utils/myconfig.h \
    utils/io_compatibility.h

INCLUDEPATH += $$PWD/../lib/
DEPENDPATH += $$PWD/../lib/

# WLanAdbLib library
LIBS += -L$$PWD/../WLanAdbLib-bin/ -lWLanAdbLib

# Protobuf library
win32: LIBS += -L$$PWD/../libs/ -lprotobuf-lite
else:unix: LIBS += -L/usr/local/lib -lprotobuf-lite
