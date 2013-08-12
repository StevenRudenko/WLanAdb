package com.wlanadb.service;

import com.wlanadb.data.ClientProto.Client;

import java.util.List;

public interface OnClientsListChanged {

    public void onClientsListChanged(List<Client> clients);
}
