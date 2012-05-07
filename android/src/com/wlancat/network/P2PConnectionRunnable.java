package com.wlancat.network;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wlancat.data.CommandProto.Command;
import com.wlancat.logcat.LogReader;
import com.wlancat.logcat.LogReaderSignalSlot;
import com.wlancat.utils.IOUtilities;

import net.sf.signalslot_apt.SignalSlot;
import net.sf.signalslot_apt.annotations.signalslot;
import net.sf.signalslot_apt.annotations.slot;

@signalslot
public class P2PConnectionRunnable implements Runnable {
  private static final String TAG = P2PConnectionRunnable.class.getSimpleName();

  public static interface ConnectionHandler {
    void onConnectionEstablished();
    boolean checkPin(String pin);
    boolean isPinRequired();
    void onConnectionClosed();
  }

  private final Socket mSocket;
  private final ConnectionHandler mConnectionHandler;

  private DataInputStream mInputStream;
  private OutputStreamWriter mOutputStream;
  private LogReader mLogReader;

  protected P2PConnectionRunnable(Socket socket, ConnectionHandler connectionHandler) {
    mConnectionHandler = connectionHandler;
    mSocket = socket;
    try {
      mSocket.setKeepAlive(true);
      mSocket.setTcpNoDelay(true);
      mSocket.setSoTimeout(250);
    } catch (SocketException e) {
      Log.e(TAG, "Fail to set Keep-Alive to socket", e);
    }
  }

  @slot
  public void onWriteLine(String line) {
    synchronized (this) {
      if (mOutputStream == null)
        return;

      try {
        mOutputStream.write(line);
        mOutputStream.write("\r\n");
        mOutputStream.flush();
      } catch (IOException e) {
        Log.e(TAG, "Fail to write line to stream: " + e.getMessage());
        close();
      }
    }
  }

  private void close() {
    if (mSocket.isClosed())
      return;

    if (mLogReader != null) {
      mLogReader.stop();
      mLogReader = null;
    }

    synchronized (this) {
      IOUtilities.closeStream(mOutputStream);
    }
    IOUtilities.closeStream(mInputStream);

    try {
      mSocket.close();
    } catch (Exception ignore) {
      Log.e(TAG, "Fail to close Socket", ignore);
    }

    mConnectionHandler.onConnectionClosed();

    Log.d(TAG, "Connection was closed");
  }

  public void run() {
    mConnectionHandler.onConnectionEstablished();

    try {
      synchronized (this) {
        mOutputStream = new OutputStreamWriter(mSocket.getOutputStream());
      }

      mInputStream = new DataInputStream(mSocket.getInputStream());

      Command cmd = null;
      Log.d(TAG, "Reading Command...");
      final ByteArrayOutputStream commandReader = new ByteArrayOutputStream();
      try {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while((bytesRead = mInputStream.read(buffer)) != -1){
          commandReader.write(buffer, 0, bytesRead);
        }
      } catch (SocketTimeoutException e) {
        Log.v(TAG, "Timeout!");
      } finally {
        commandReader.flush();
        if (commandReader.size() == 0)
          IOUtilities.closeStream(commandReader);
        else {
          try {
            Log.d(TAG, "Parsing Command...");
            cmd = Command.parseFrom(commandReader.toByteArray());
          } catch (InvalidProtocolBufferException e) {
            Log.w(TAG, "Fail to parse command", e);
          } finally {
            IOUtilities.closeStream(commandReader);
          }
        }
      }

      if (cmd != null) {
        Log.d(TAG, "Command recieved: ");
        Log.d(TAG, " - params: " + cmd.getParams());
        Log.d(TAG, " - pin: " + cmd.getPin());

        if (mConnectionHandler.isPinRequired()) {
          // pin was not provided to connect with device. terminating connection.
          if (!cmd.hasPin()) {
            close();
            return;
          }

          // if pin is not correct. terminating connection
          if (!mConnectionHandler.checkPin(cmd.getPin())) {
            close();
            return;
          }
        }
      }

      mLogReader = new LogReaderSignalSlot();
      SignalSlot.connect(mLogReader, LogReaderSignalSlot.Signals.ONLOGMESSAGE_STRING, this, P2PConnectionRunnableSignalSlot.Slots.ONWRITELINE_STRING);

      mLogReader.start();
    } catch (IOException e) {
      Log.e(TAG, "Error while communicating with client", e);
    } finally {
      close();
    }
  }
}