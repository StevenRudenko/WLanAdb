package com.wlancat.service;

interface WLanServiceApi {
  int getPort();
  String getAddress();
  /**
  */
  int getConnectionsCount();
}