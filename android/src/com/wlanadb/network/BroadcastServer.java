package com.wlanadb.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.wlanadb.config.MyConfig;

public class BroadcastServer implements Runnable {
  private static final String TAG = BroadcastServer.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  public interface BroadcastMessageHandler {
    public ByteString onDataPackageRecieved(ByteString data);
  }

  public static final int BROADCAST_PORT = 44533;
  public static final int MESSAGE_LISTEN_TIMEOUT = 500;
  public static final int MESSAGE_BUFFER = 1000;

  private InetAddress mBroadcastAddress;
  private InetAddress mLocalAddress;

  private Thread mRecieveThread;
  private DatagramSocket mSocket;

  private BroadcastMessageHandler mHandler;

  private MulticastLock mMulticastLock;
  private volatile boolean isRunning = false;

  public BroadcastServer(BroadcastMessageHandler handler) {
    mHandler = handler;
  }

  public void start(WifiManager wifiManager, InetAddress broadcastAddress, InetAddress localAddress) {
    mMulticastLock = wifiManager.createMulticastLock(TAG);
    mMulticastLock.acquire();

    mBroadcastAddress = broadcastAddress;
    mLocalAddress = localAddress;
    if (DEBUG) {
      Log.d(TAG, "Updating addresses:");
      Log.d(TAG, "- broadcast address: " + mBroadcastAddress.getHostAddress());
      Log.d(TAG, "- local address: " + mLocalAddress.getHostAddress());
      Log.d(TAG, "- port: " + BROADCAST_PORT);
    }

    isRunning = true;

    try {
      mSocket = new DatagramSocket(BROADCAST_PORT);
      mSocket.setBroadcast(true);
      mSocket.setSoTimeout(MESSAGE_LISTEN_TIMEOUT);
    } catch (IOException e) {
      Log.e(TAG, "Could not open socket", e);
    }

    isRunning = true;

    synchronized (this) {
      mRecieveThread = new Thread(this);
      mRecieveThread.start();
    }
  }

  public void stop() {
    if (DEBUG)
      Log.d(TAG, "Closing socket...");

    isRunning = false;

    mSocket.close();

    if (mMulticastLock != null && mMulticastLock.isHeld()) {
      mMulticastLock.release();
      mMulticastLock = null;
    }
  }

  public void send(final ByteString data, final InetAddress reciever) {
    new Thread(TAG+":SEND") {
      @Override
      public void run() {
        super.run();
        try {
          final byte[] message = data.toByteArray();
          final InetAddress host = reciever == null ? mBroadcastAddress : reciever;
          final DatagramPacket packet = new DatagramPacket(message, message.length,
              host, BROADCAST_PORT);
          if (isRunning)
            mSocket.send(packet);
        } catch (IOException e) {
          Log.e(TAG, "Failed broadcast data", e);
        }
      }
    }.start();
  }

  /**
   * Send a UDP packet to specific address. If address is broadcast address then message will be send broadcast.
   */
  public void send(final byte[] data, final InetAddress address) {
    new Thread(TAG+":SEND") {
      @Override
      public void run() {
        super.run();
        try {
          final InetAddress reciever = address == null ? mBroadcastAddress : address;
          if (DEBUG)
            Log.d(TAG, "Sending data to " + reciever.getHostAddress());
          final DatagramPacket packet = new DatagramPacket(data, data.length,
              reciever, BROADCAST_PORT);
          if (isRunning)
            mSocket.send(packet);
        } catch (IOException e) {
          Log.e(TAG, "Data send failed to " + 
              (address == null ?  "ALL" : address.getHostAddress()), e);
        }
      }
    }.start();
  }

  public void run() {
    if (DEBUG)
      Log.d(TAG, "Starting reciever loop...");
    isRunning = true;

    final String localHostAddress = mLocalAddress.getHostAddress();
    final byte[] buf = new byte[MESSAGE_BUFFER];
    // Loop and try to receive messages. We'll get back the packet we just
    // sent out, which isn't terribly helpful, but we'll discard it.
    while (isRunning) {
      if (mSocket.isClosed())
        return;

      final DatagramPacket packet = new DatagramPacket(buf, buf.length);
      try {
        mSocket.receive(packet);
      } catch (SocketTimeoutException e) {
        // it is normal behavior
        continue;
      } catch (IOException e) {
        // this error can be caused by closing socket only
        // ignoring it
        continue;
      }

      final InetAddress senderAddress = packet.getAddress();
      final String senderHostAddress = senderAddress.getHostAddress();

      // ignoring packages from ourself
      if (localHostAddress.equals(senderHostAddress))
        continue;

      final int packetLength = packet.getLength();
      final byte[] packetData = packet.getData();

      final ByteString data = ByteString.copyFrom(packetData, 0, packetLength);
      if (DEBUG)
        Log.d(TAG, "Recieved message from " + senderHostAddress + " : " + data.toStringUtf8());
      final ByteString response = mHandler.onDataPackageRecieved(data);
      if (response != null)
        send(response, senderAddress);
    }

    stop();
  }
}
