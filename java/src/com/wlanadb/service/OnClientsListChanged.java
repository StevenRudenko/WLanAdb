package com.wlanadb.service;

import java.util.List;

import com.wlanadb.data.ClientProto.Client;

public interface OnClientsListChanged {

  public void onClientsListChanged(List<Client> clients);
}
