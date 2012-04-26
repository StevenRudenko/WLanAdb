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
        .setName(Build.DEVICE)
        .build();

    Log.v(TAG, "Request message: [" + this + "]");
  }

  @slot
  public void onMessageRecieved(ByteString data) {
    //    try {
    //      final Message message = Message.newBuilder().mergeFrom(data).buildPartial();
    //
    //      if (message.getType() != Message.Type.REQEST)
    //        return;
    //
    //      final Message response = Message.newBuilder()
    //          .setType(Message.Type.RESPONSE)
    //          .setClient(mLocalClient)
    //          .build();
    //      sendResponse(response.toByteString());
    //    } catch (InvalidProtocolBufferException e) {
    //      Log.e(TAG, "Can't parse client!", e);
    //    }
    Log.v(TAG, "Recieved: " + data.toStringUtf8());
    final Message response = Message.newBuilder()
        .setType(Message.Type.RESPONSE)
        .setClient(mLocalClient)
        .build();
    sendResponse(response.toByteString());
  }

  @signal
  public abstract void sendResponse(ByteString data);

  @Override
  public String toString() {
    final Message request = Message.newBuilder().setType(Message.Type.REQEST).build();
    return request.toString();
  }
}
