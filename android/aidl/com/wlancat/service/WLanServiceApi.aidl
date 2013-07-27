package com.wlancat.service;

interface WLanServiceApi {
  /** Returns port of P2P server. */
  int getPort();
  /** Returns IP address of device. */
  String getAddress();
  /** Returns count of connections established right now. */
  int getConnectionsCount();
  /** Performs test of connection. */
  void testConnection();
}