package com.wlancat.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

import android.util.Log;

import com.wlancat.logcat.LogReader;
import com.wlancat.logcat.LogReaderSignalSlot;
import com.wlancat.utils.IOUtilities;

import net.sf.signalslot_apt.SignalSlot;
import net.sf.signalslot_apt.annotations.signalslot;
import net.sf.signalslot_apt.annotations.slot;

@signalslot
public class P2PConnectionRunnable implements Runnable {
  private static final String TAG = P2PConnectionRunnable.class.getSimpleName();

  public static interface ConnectionStateListener {
    void onConnectionEstablished();
    void onConnectionClosed();
  }

  private final Socket mSocket;

  private DataInputStream mInputStream;
  private OutputStreamWriter mOutputStream;
  private LogReader mLogReader;

  private ConnectionStateListener mConnectionStateListener;

  protected P2PConnectionRunnable(Socket socket) {
    mSocket = socket;
    try {
      mSocket.setKeepAlive(true);
      mSocket.setTcpNoDelay(true);
    } catch (SocketException e) {
      Log.e(TAG, "Fail to set Keep-Alive to socket", e);
    }
  }

  public ConnectionStateListener getConnectionStateListener() {
    return mConnectionStateListener;
  }

  public void setConnectionStateListener(ConnectionStateListener listener) {
    mConnectionStateListener = listener;
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

    if (mConnectionStateListener != null)
      mConnectionStateListener.onConnectionClosed();

    Log.d(TAG, "Connection was closed");
  }

  public void run() {
    if (mConnectionStateListener != null)
      mConnectionStateListener.onConnectionEstablished();

    try {
      synchronized (this) {
        mOutputStream = new OutputStreamWriter(mSocket.getOutputStream());
      }
      mInputStream = new DataInputStream(mSocket.getInputStream());

      //TODO: prepare log command depending on request

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