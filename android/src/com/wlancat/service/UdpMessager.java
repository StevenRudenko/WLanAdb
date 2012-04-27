package com.wlancat.service;

import java.net.InetAddress;

import net.sf.signalslot_apt.annotations.signal;
import net.sf.signalslot_apt.annotations.signalslot;
import net.sf.signalslot_apt.annotations.slot;
import android.os.Build;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wlancat.data.ClientProto.Client;
import com.wlancat.data.MessageProto.Message;

@signalslot(force_concrete=true)
public abstract class UdpMessager {
  private static final String TAG = UdpMessager.class.getSimpleName();

  private final Client mLocalClient;

  public UdpMessager(InetAddress localAddress, int port) {
    mLocalClient = Client.newBuilder()
        .setIp(localAddress.getHostAddress())
        .setPort(port)
        .setName(Build.MODEL)
        .build();
  }

  @slot
  public void onMessageRecieved(ByteString data) {
    try {
      final Message message = Message.newBuilder().mergeFrom(data).buildPartial();

      if (!message.getType().equals(Message.Type.REQEST))
        return;

      final Message response = Message.newBuilder()
          .setType(Message.Type.RESPONSE)
          .setClient(mLocalClient)
          .build();
      sendResponse(response.toByteString());
    } catch (InvalidProtocolBufferException e) {
      Log.e(TAG, "Can't parse client!", e);
    }
  }

  @signal
  public abstract void sendResponse(ByteString data);
}
