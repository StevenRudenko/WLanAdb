#-------------------------------------------------
#
# Project created by QtCreator 2012-09-13T15:54:11
#
#-------------------------------------------------

QT       += core gui network

TARGET = WLanAdbUI
TEMPLATE = app

SOURCES += main.cpp\
        deviceswindow.cpp \
    opendialog.cpp

HEADERS  += deviceswindow.h \
    utils/myconfig.h \
    opendialog.h

FORMS    += deviceswindow.ui

RESOURCES += \
    main.qrc

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

OTHER_FILES += \
    images/icon_lock.png
