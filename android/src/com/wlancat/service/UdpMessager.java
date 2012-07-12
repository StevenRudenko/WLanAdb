package com.wlancat.service;

import net.sf.signalslot_apt.annotations.signal;
import net.sf.signalslot_apt.annotations.signalslot;
import net.sf.signalslot_apt.annotations.slot;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wlancat.data.ClientProto.Client;
import com.wlancat.data.MessageProto.Message;

@signalslot(force_concrete=true)
public abstract class UdpMessager {
  private static final String TAG = UdpMessager.class.getSimpleName();

  private Client mLocalClient;

  public UdpMessager(Client client) {
    mLocalClient = client;
  }

  @slot
  public void onClientChanged(Client client) {
    synchronized (this) {
      mLocalClient = client;
    }
  }

  @slot
  public void onMessageRecieved(ByteString data) {
    try {
      final Message message = Message.parseFrom(data);

      if (!message.getType().equals(Message.Type.REQEST))
        return;

      final Message response;
      synchronized (this) {
        response = Message.newBuilder()
            .setType(Message.Type.RESPONSE)
            .setClient(mLocalClient)
            .build();
      }
      sendResponse(response.toByteString());
    } catch (InvalidProtocolBufferException e) {
      Log.e(TAG, "Can't parse client!", e);
    }
  }

  @signal
  public abstract void sendResponse(ByteString data);
}
