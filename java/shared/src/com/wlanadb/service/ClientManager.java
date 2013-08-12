package com.wlanadb.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wlanadb.config.MyConfig;
import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.data.MessageProto.Message;
import com.wlanadb.network.BroadcastServer.BroadcastMessageHandler;
import com.wlanadb.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientManager implements BroadcastMessageHandler {
    private static final String TAG = ClientManager.class.getSimpleName();

    private final HashMap<String, Client> clients = new HashMap<String, Client>();
    private final OnClientsListChanged listener;

    public ClientManager(OnClientsListChanged listener) {
        this.listener = listener;
    }

    public List<Client> getClients() {
        return new ArrayList<Client>(clients.values());
    }

    @Override
    public ByteString onDataPackageRecieved(ByteString data) {
        try {
            final Message message = Message.parseFrom(data);

            if (!message.getType().equals(Message.Type.RESPONSE))
                return null;

            final Client client = message.getClient();
            clients.put(client.getId(), client);

            if (MyConfig.DEBUG)
                Log.d(TAG, "Client was added: " + client.getId() + " " + client.getIp());
            listener.onClientsListChanged(getClients());
        } catch (InvalidProtocolBufferException e) {
            Log.e(TAG, "Can't parse message!", e);
        }

        return null;
    }

}
