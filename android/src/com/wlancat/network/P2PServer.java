package com.wlancat.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import android.util.Log;

public class P2PServer implements Runnable {
  private static final String TAG = P2PServer.class.getSimpleName();

  private static final int MAX_CLIENTS_AT_TIME = 2;

  private final ExecutorService mClientsHandler = Executors.newFixedThreadPool(MAX_CLIENTS_AT_TIME);

  private ServerSocket mServerSocket;
  private Thread mListenThread;

  private volatile boolean isRunning = false;

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
        mClientsHandler.execute(new P2PConnectionRunnableSignalSlot(socket));
      } catch (RejectedExecutionException e) {
        Log.d(TAG, "There is no available slots to handle connection!");
        try {
          socket.close();
        } catch (Exception ignore) {
        }
      }
    }
  }
}
