#-------------------------------------------------
#
# Project created by QtCreator 2012-04-26T23:02:04
#
#-------------------------------------------------

QT       += core

QT       -= gui

TARGET = WLanCat
CONFIG   += console
CONFIG   -= app_bundle

TEMPLATE = app

LIBS += -L/usr/local/lib -lprotobuf

SOURCES += main.cpp \
    message.pb.cc \
    client.pb.cc

HEADERS += \
    message.pb.h \
    client.pb.h
