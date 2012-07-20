package com.wlancat.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.SocketException;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wlancat.config.MyConfig;
import com.wlancat.data.CommandProto.Command;
import com.wlancat.utils.IOUtilities;
import com.wlancat.worker.BaseWorker;
import com.wlancat.worker.CommandListener;
import com.wlancat.worker.CommandProcessor;

public class P2PConnection extends CommandListener implements Runnable {
  private static final String TAG = P2PConnection.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  private final Socket mSocket;

  private WeakReference<CommandProcessor> mCommandProcessor;

  private Command command;
  private BaseWorker worker;

  /* Write the image to disk first. Then try to open sockets 
   * in parallel each transferring a different offset of the
   * image (e.g. split the image to 3 jobs, transfer each in
   * parallel to the others). This will workaround some TCP
   * behaviors.
   */
  protected P2PConnection(Socket socket, CommandProcessor commandProcessor) {
    super(commandProcessor);

    mSocket = socket;
    try {
      mSocket.setKeepAlive(true);
      mSocket.setTcpNoDelay(true);
    } catch (SocketException e) {
      Log.e(TAG, "Fail to set Keep-Alive to socket", e);
    }
  }

  public void setCommandProcessor(CommandProcessor commandProcessor) {
    mCommandProcessor = new WeakReference<CommandProcessor>(commandProcessor);
  }

  private void close() {
    if (mSocket.isClosed())
      return;

    if (worker != null) {
      worker.terminate();
      worker = null;
    }

    try {
      IOUtilities.closeStream(mSocket.getOutputStream());
    } catch (IOException e) {
      // do nothing
    }

    try{
      IOUtilities.closeStream(mSocket.getInputStream());
    } catch (IOException e) {
      // do nothing
    }

    try {
      mSocket.close();
    } catch (Exception ignore) {
      Log.e(TAG, "Fail to close Socket", ignore);
    }

    if (DEBUG)
      Log.d(TAG, "Connection was closed");
  }

  public void run() {
    try {
      final DataInputStream mInputStream = new DataInputStream(mSocket.getInputStream());
      final int commandLength = mInputStream.readInt();

      if (DEBUG)
        Log.d(TAG, "Command size: " + commandLength);

      byte[] buffer = new byte[commandLength];
      mInputStream.read(buffer);

      try {
        Log.d(TAG, "Parsing Command...");
        command = Command.parseFrom(buffer);
        buffer = null;
      } catch (InvalidProtocolBufferException e) {
        Log.w(TAG, "Fail to parse command", e);
      }

      CommandProcessor commandProcessor = mCommandProcessor.get();
      if (commandProcessor == null) {
        close();
        return;
      }

      worker = commandProcessor.getWorker(command);
      if (worker == null) {
        close();
        return;
      }

      worker.setInputStream(mSocket.getInputStream());
      worker.setOutputStream(mSocket.getOutputStream());
      worker.execute();
    } catch (IOException e) {
      Log.e(TAG, "Error while communicating with client", e);
    } finally {
      close();
    }
  }
}