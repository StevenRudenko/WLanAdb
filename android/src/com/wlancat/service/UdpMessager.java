package com.wlancat.service;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wlancat.data.ClientProto.Client;
import com.wlancat.data.ClientSettings.OnClientChangeListener;
import com.wlancat.data.MessageProto.Message;
import com.wlancat.network.BroadcastServer.BroadcastMessageHandler;

public class UdpMessager implements OnClientChangeListener, BroadcastMessageHandler {
  private static final String TAG = UdpMessager.class.getSimpleName();

  private Client mLocalClient;

  @Override
  public void onClientChanged(Client client) {
    synchronized (this) {
      mLocalClient = client;
    }
  }

  @Override
  public ByteString onDataPackageRecieved(ByteString data) {
    try {
      final Message message = Message.parseFrom(data);

      if (!message.getType().equals(Message.Type.REQEST))
        return null;

      final Message response;
      synchronized (this) {
        response = Message.newBuilder()
            .setType(Message.Type.RESPONSE)
            .setClient(mLocalClient)
            .build();
      }
      return response.toByteString();
    } catch (InvalidProtocolBufferException e) {
      Log.e(TAG, "Can't parse client!", e);
    }

    return null;
  }
}
