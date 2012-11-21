#-------------------------------------------------
#
# Project created by QtCreator 2012-04-26T23:02:04
#
#-------------------------------------------------

QT       += core, network
QT       -= gui

TARGET = WLanAdbTerminal
CONFIG   += console
CONFIG   += static
CONFIG   -= app_bundle

TEMPLATE = app

SOURCES += main.cpp \
    adapter/adapter.cpp \
    adapter/installadapter.cpp \
    adapter/logcatadapter.cpp \
    adapter/pushadapter.cpp \
    utils/io_compatibility.cpp \
    wlanadbterminal.cpp \
    utils/myconfig.cpp

HEADERS += \
    adapter/adapter.h \
    adapter/installadapter.h \
    adapter/logcatadapter.h \
    adapter/pushadapter.h \
    utils/myconfig.h \
    utils/io_compatibility.h \
    wlanadbterminal.h

INCLUDEPATH += $$PWD../lib/protobuf-include
DEPENDPATH += $$PWD../lib/protobuf-include

INCLUDEPATH += $$PWD/../lib/
DEPENDPATH += $$PWD/../lib/

# WLanAdbLib library
win32: LIBS += -L$$PWD/../WLanAdbLib-bin/release/ -lWLanAdbLib
else:unix: LIBS += -L$$PWD/../WLanAdbLib-bin/ -lWLanAdbLib

# Protobuf library
win32: LIBS += -L$$PWD/../libs/ -lprotobuf-lite
else:unix: LIBS += -L/usr/local/lib -lprotobuf-lite
