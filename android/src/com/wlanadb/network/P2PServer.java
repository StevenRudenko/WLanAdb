package com.wlanadb.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.wlanadb.config.MyConfig;
import com.wlanadb.worker.CommandListener;
import com.wlanadb.worker.CommandProcessor;

import android.util.Log;

public class P2PServer extends CommandListener implements Runnable {
    private static final String TAG = P2PServer.class.getSimpleName();
    private static final boolean DEBUG = MyConfig.DEBUG && true;

    public interface OnConnectionsCountChanged {
        public void onConnectionsCountChanged(int connectionsCount);
    }

    private static final int MAX_CONNECTIONS = Runtime.getRuntime().availableProcessors() * 2;

    private final ExecutorService mConnectionsPool = new ConnectionsPoolExecutor();

    private ServerSocket mServerSocket;
    private Thread mListenThread;

    private int mActiveConnections = 0;

    private OnConnectionsCountChanged mListener;

    private volatile boolean isRunning = false;

    public P2PServer(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    public int start(OnConnectionsCountChanged listener) {
        mListener = listener;

        isRunning = true;

        try {
            // free port will be assigned to a socket
            mServerSocket = new ServerSocket(0);
            mServerSocket.setReuseAddress(true);
        } catch (IOException e) {
            if (DEBUG)
                Log.e(TAG, "Can't open server socket connection", e);
            return -1;
        }

        mListenThread = new Thread(this);
        mListenThread.start();

        return mServerSocket.getLocalPort();
    }

    public void stop() {
        mListener = null;

        isRunning = false;

        // wait for all of the executor threads to finish
        mConnectionsPool.shutdownNow();

        if (mListenThread != null) {
            final Thread inherited = mListenThread;
            mListenThread = null;
            inherited.interrupt();
        }

        try {
            mServerSocket.close();
        } catch (IOException e) {
            if (DEBUG)
                Log.e(TAG, "Can't close server socket", e);
        }
    }

    public int getPort() {
        return mServerSocket == null ? -1 : mServerSocket.getLocalPort();
    }

    public int getActiveConnectionsCount() {
        synchronized (mConnectionsPool) {
            return mActiveConnections;
        }
    }

    public void run() {
        if (DEBUG)
            Log.v(TAG, "Waiting for clients connection...");

        while (isRunning) {
            final Socket socket;
            try {
                if (mServerSocket.isClosed())
                    break;

                socket = mServerSocket.accept();
            } catch (SocketException e) {
                if (isRunning) {
                    if (DEBUG)
                        Log.e(TAG, "Failed to accept connection with client.", e);
                    continue;
                } else {
                    if (DEBUG)
                        Log.w(TAG, "Cancel waiting for a clients because on closing connection.");
                    break;
                }
            } catch (IOException e) {
                if (DEBUG)
                    Log.e(TAG, "Failed to accept connection with client", e);
                continue;
            }

            try {
                if (DEBUG)
                    Log.v(TAG, "New client asked for a connection");
                final P2PConnection connection = new P2PConnection(socket, getCommandProcessor());
                mConnectionsPool.execute(connection);
            } catch (RejectedExecutionException e) {
                if (DEBUG)
                    Log.w(TAG, "There is no available slots to handle connection!");
                try {
                    socket.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void setActiveConnectionsCount(int count) {
        synchronized (mConnectionsPool) {
            if (mActiveConnections == count)
                return;

            if (DEBUG)
                Log.v(TAG, "Active connections: " + count);
            mActiveConnections = count;
            if (mListener != null)
                mListener.onConnectionsCountChanged(mActiveConnections);
        }
    }

    private class ConnectionsPoolExecutor extends ThreadPoolExecutor {
        public ConnectionsPoolExecutor() {
            super(MAX_CONNECTIONS, // core thread pool size
                    MAX_CONNECTIONS, // maximum thread pool size
                    60, // time to wait before resizing pool
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(MAX_CONNECTIONS, true),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }



        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);

            setActiveConnectionsCount(getActiveCount());
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);

            setActiveConnectionsCount(getActiveCount() - 1);
        }
    }
}
