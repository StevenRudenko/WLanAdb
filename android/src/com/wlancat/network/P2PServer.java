package com.wlancat.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import net.sf.signalslot_apt.annotations.signal;
import net.sf.signalslot_apt.annotations.signalslot;

import android.util.Log;

@signalslot(force_concrete=true)
public abstract class P2PServer implements Runnable {
  private static final String TAG = P2PServer.class.getSimpleName();

  private static final int MAX_CLIENTS_AT_TIME = 2;

  private final ExecutorService mClientsHandler = Executors.newFixedThreadPool(MAX_CLIENTS_AT_TIME);

  private ServerSocket mServerSocket;
  private Thread mListenThread;

  private int mActiveConnections = 0;

  private volatile boolean isRunning = false;

  @signal
  public abstract void onActiveConnectionsCountChanged(int connectionsCount);

  public int start() {
    isRunning = true;

    try {
      // free port will be assigned to a socket
      mServerSocket = new ServerSocket(0);
    } catch (IOException e) {
      Log.d(TAG, "Can't open server socket connection", e);
      return -1;
    }

    mListenThread = new Thread(this);
    mListenThread.start();

    return mServerSocket.getLocalPort();
  }

  public void stop() {
    isRunning = false;

    mClientsHandler.shutdownNow();

    if (mListenThread != null) {
      final Thread inherited = mListenThread;
      mListenThread = null;
      inherited.interrupt();
    }

    try {
      mServerSocket.close();
    } catch (IOException e) {
      Log.e(TAG, "Can't close server socket", e);
    }
  }

  public int getPort() {
    return mServerSocket == null ? -1 : mServerSocket.getLocalPort();
  }

  public int getActiveConnectionsCount() {
    synchronized (mConnectionStateListener) {
      return mActiveConnections;
    }
  }

  public void run() {
    Log.d(TAG, "Waiting for clients connection...");

    while (isRunning) {
      final Socket socket;
      try {
        if (mServerSocket.isClosed())
          break;

        socket = mServerSocket.accept();
      } catch (SocketException e) {
        if (isRunning) {
          Log.e(TAG, "Failed to accept connection with client.", e);
          continue;
        } else {
          Log.w(TAG, "Cancel waiting for a clients because on closing connection.");
          break;
        }
      } catch (IOException e) {
        Log.e(TAG, "Failed to accept connection with client", e);
        continue;
      }

      try {
        Log.d(TAG, "New client asked for a connection");
        final P2PConnectionRunnable connection = new P2PConnectionRunnableSignalSlot(socket);
        connection.setConnectionStateListener(mConnectionStateListener);
        mClientsHandler.execute(connection);
      } catch (RejectedExecutionException e) {
        Log.d(TAG, "There is no available slots to handle connection!");
        try {
          socket.close();
        } catch (Exception ignore) {
        }
      }
    }
  }

  private void setActiveConnectionsCount(int count) {
    synchronized (mConnectionStateListener) {
      if (mActiveConnections == count)
        return;

      Log.d(TAG, "Active connections: " + count);
      mActiveConnections = count;
      onActiveConnectionsCountChanged(mActiveConnections);
    }
  }

  private final P2PConnectionRunnable.ConnectionStateListener mConnectionStateListener = new P2PConnectionRunnable.ConnectionStateListener() {
    @Override
    public void onConnectionEstablished() {
      setActiveConnectionsCount(mActiveConnections+1);
    }

    @Override
    public void onConnectionClosed() {
      setActiveConnectionsCount(mActiveConnections-1);
    }
  };
}
