package com.wlanadb.network;

import com.wlanadb.config.MyConfig;
import com.wlanadb.utils.Log;

import java.io.IOException;
import java.net.*;

/**
 * Created with IntelliJ IDEA.
 * User: steven
 * Date: 8/5/13
 * Time: 7:03 PM
 */
public class SingleInstanceChecker {
    private static final String TAG = SingleInstanceChecker.class.getSimpleName();
    private static final boolean DEBUG = MyConfig.DEBUG && true;

    private static final int CHECK_PORT = 49152;

    public interface OnSecondInstanceStartListener {
        public void onSecondInstanceStarted();
    }

    public static class MainInstance implements Runnable {
        private ServerSocket mServerSocket;
        private Thread mListenThread;

        private final OnSecondInstanceStartListener listener;

        private volatile boolean isRunning = false;

        public MainInstance(OnSecondInstanceStartListener listener) {
            this.listener = listener;
        }

        public void start() {
            try {
                mServerSocket = new ServerSocket(CHECK_PORT);
            } catch (IOException e) {
                if (DEBUG)
                    Log.e(TAG, "Can't open server socket connection", e);
            }

            isRunning = true;

            mListenThread = new Thread(this);
            mListenThread.start();

            if (DEBUG)
                Log.v(TAG, "Start listening for children.");
        }

        public void stop() {
            if (!isRunning && mServerSocket.isClosed())
                return;

            isRunning = false;

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
            if (DEBUG)
                Log.v(TAG, "Socket closed.");
        }

        @Override
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
                    listener.onSecondInstanceStarted();
                    socket.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public static class ChildInstance {
        public static void notifyMain() {
            Socket socket = null;
            try {
                socket = new Socket(InetAddress.getLocalHost(), CHECK_PORT);
            } catch (UnknownHostException e) {
                if (DEBUG)
                    Log.e(TAG, "Can't connect to client to main instance: " + CHECK_PORT, e);
            } catch (IOException e) {
                if (DEBUG)
                    Log.e(TAG, "Can't open streams to read/write data", e);
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }
}
